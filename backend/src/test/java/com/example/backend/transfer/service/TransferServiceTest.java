package com.example.backend.transfer.service;

import com.example.backend.account.entity.Account;
import com.example.backend.account.entity.AccountStatus;
import com.example.backend.account.entity.AccountType;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.customer.entity.Customer;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.entity.TransactionType;
import com.example.backend.transaction.repository.TransactionRepository;
import com.example.backend.transfer.dto.TransferRequest;
import com.example.backend.transfer.dto.TransferResponse;
import com.example.backend.transfer.entity.Transfer;
import com.example.backend.transfer.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String SUBJECT = "auth0|sender";
    private static final String FROM = "0000000001";
    private static final String TO = "0000000002";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferMovesMoneyAndRecordsBothSides() {
        Account from = account(FROM, "100.00", SUBJECT);
        Account to = account(TO, "20.00", "auth0|recipient");
        when(accountRepository.findByAccountNumber(FROM)).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber(TO)).thenReturn(Optional.of(to));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 12L);
            return saved;
        });

        TransferResponse response = transferService.transfer(request("30.00"), SUBJECT);

        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("50.00"), to.getBalance());
        assertEquals(12L, response.id());
        assertEquals(FROM, response.fromAccount().accountNumber());
        assertEquals(TO, response.toAccount().accountNumber());
        assertEquals(new BigDecimal("30.00"), response.amount());
        assertNotNull(response.createdAt());

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        assertSame(from, transferCaptor.getValue().getFromAccount());
        assertSame(to, transferCaptor.getValue().getToAccount());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        List<Transaction> transactions = transactionCaptor.getAllValues();
        assertEquals(TransactionType.DEBIT, transactions.get(0).getType());
        assertSame(from, transactions.get(0).getAccount());
        assertEquals(new BigDecimal("70.00"), transactions.get(0).getBalanceAfter());
        assertEquals(TransactionType.CREDIT, transactions.get(1).getType());
        assertSame(to, transactions.get(1).getAccount());
        assertEquals(new BigDecimal("50.00"), transactions.get(1).getBalanceAfter());
        for (Transaction transaction : transactions) {
            assertSame(transferCaptor.getValue(), transaction.getTransfer());
            assertEquals(new BigDecimal("30.00"), transaction.getAmount());
            assertEquals("Shared expenses", transaction.getReference());
        }
    }

    @Test
    void transferRejectsSameSourceAndDestinationBeforeLookup() {
        TransferRequest request = new TransferRequest(FROM, FROM, new BigDecimal("10.00"), null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transfer(request, SUBJECT));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("from and to accounts must differ", exception.getReason());
        verifyNoInteractions(accountRepository, transferRepository, transactionRepository);
    }

    @Test
    void transferRejectsAmountWithMoreThanTwoDecimalPlaces() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transfer(request("10.001"), SUBJECT));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Amount can have at most two decimal places", exception.getReason());
        verifyNoInteractions(accountRepository, transferRepository, transactionRepository);
    }

    @Test
    void transferRejectsMissingDestinationAccount() {
        Account from = account(FROM, "100.00", SUBJECT);
        when(accountRepository.findByAccountNumber(FROM)).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber(TO)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transfer(request("10.00"), SUBJECT));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found: " + TO, exception.getReason());
        assertEquals(new BigDecimal("100.00"), from.getBalance());
        verifyNoInteractions(transferRepository, transactionRepository);
    }

    @Test
    void transferRejectsSourceAccountOwnedByAnotherCustomer() {
        Account from = account(FROM, "100.00", "auth0|someone-else");
        Account to = account(TO, "20.00", SUBJECT);
        when(accountRepository.findByAccountNumber(FROM)).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber(TO)).thenReturn(Optional.of(to));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transfer(request("10.00"), SUBJECT));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Source account not found", exception.getReason());
        assertEquals(new BigDecimal("100.00"), from.getBalance());
        assertEquals(new BigDecimal("20.00"), to.getBalance());
        verifyNoInteractions(transferRepository, transactionRepository);
    }

    @Test
    void transferRejectsInactiveAccount() {
        Account from = account(FROM, "100.00", SUBJECT);
        Account to = account(TO, "20.00", SUBJECT);
        ReflectionTestUtils.setField(to, "status", AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber(FROM)).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber(TO)).thenReturn(Optional.of(to));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transfer(request("10.00"), SUBJECT));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Both accounts must be active", exception.getReason());
        assertEquals(new BigDecimal("100.00"), from.getBalance());
        assertEquals(new BigDecimal("20.00"), to.getBalance());
        verifyNoInteractions(transferRepository, transactionRepository);
    }

    @Test
    void transferRejectsInsufficientFundsWithoutChangingBalances() {
        Account from = account(FROM, "5.00", SUBJECT);
        Account to = account(TO, "20.00", SUBJECT);
        when(accountRepository.findByAccountNumber(FROM)).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber(TO)).thenReturn(Optional.of(to));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transfer(request("10.00"), SUBJECT));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Insufficient funds", exception.getReason());
        assertEquals(new BigDecimal("5.00"), from.getBalance());
        assertEquals(new BigDecimal("20.00"), to.getBalance());
        verifyNoInteractions(transferRepository, transactionRepository);
    }

    private Account account(String number, String balance, String subject) {
        return new Account(number, "Demo account", new BigDecimal(balance), AccountType.CHECKING,
                new Customer(subject, subject + "@example.com"));
    }

    private TransferRequest request(String amount) {
        return new TransferRequest(FROM, TO, new BigDecimal(amount), "Shared expenses");
    }
}
