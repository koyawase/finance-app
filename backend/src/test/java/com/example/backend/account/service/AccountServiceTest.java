package com.example.backend.account.service;

import com.example.backend.account.dto.AccountCreateRequest;
import com.example.backend.account.dto.AccountResponse;
import com.example.backend.account.entity.Account;
import com.example.backend.account.entity.AccountStatus;
import com.example.backend.account.entity.AccountType;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.customer.entity.Customer;
import com.example.backend.customer.repository.CustomerRepository;
import com.example.backend.transaction.dto.TransactionResponse;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.entity.TransactionType;
import com.example.backend.transaction.repository.TransactionRepository;
import com.example.backend.transfer.entity.Transfer;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final String SUBJECT = "auth0|customer-1";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void listAccountsReturnsOnlyAccountsFoundForSubject() {
        Account account = account("0000000001", "Everyday", "100.00");
        ReflectionTestUtils.setField(account, "id", 7L);
        when(accountRepository.findByCustomer_Auth0Subject(SUBJECT)).thenReturn(List.of(account));

        List<AccountResponse> responses = accountService.getAccountsForSubject(SUBJECT);

        assertEquals(1, responses.size());
        assertEquals(7L, responses.get(0).id());
        assertEquals("0000000001", responses.get(0).accountNumber());
        assertEquals("Everyday", responses.get(0).accountName());
        assertEquals(new BigDecimal("100.00"), responses.get(0).balance());
        assertEquals(AccountStatus.ACTIVE, responses.get(0).status());
        verify(accountRepository).findByCustomer_Auth0Subject(SUBJECT);
    }

    @Test
    void createAccountLinksCustomerAndGeneratesActiveAccount() {
        Customer customer = new Customer(SUBJECT, "alex@example.com");
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.of(customer));
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 42L);
            return saved;
        });

        AccountResponse response = accountService.createAccountForSubject(SUBJECT,
                new AccountCreateRequest("  Travel fund  ", AccountType.SAVINGS, new BigDecimal("250.00")));

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account saved = accountCaptor.getValue();
        assertEquals(customer, saved.getCustomer());
        assertEquals("Travel fund", saved.getAccountName());
        assertEquals(AccountType.SAVINGS, saved.getAccountType());
        assertEquals(new BigDecimal("250.00"), saved.getBalance());
        assertEquals(AccountStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.getAccountNumber().matches("[0-9]{10}"));
        verify(accountRepository).findByAccountNumber(saved.getAccountNumber());
        assertEquals(42L, response.id());
        assertEquals(saved.getAccountNumber(), response.accountNumber());
        assertEquals(AccountStatus.ACTIVE, response.status());
    }

    @Test
    void createAccountRequiresLinkedCustomer() {
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.createAccountForSubject(SUBJECT,
                        new AccountCreateRequest("Savings", AccountType.SAVINGS, BigDecimal.ZERO)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(accountRepository);
    }

    @Test
    void listTransactionsRejectsAccountNotOwnedBySubject() {
        when(accountRepository.findByIdAndCustomer_Auth0Subject(7L, SUBJECT)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.getTransactionsByAccountId(7L, SUBJECT));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void listTransactionsMapsTransferAndOpeningCredit() {
        Account from = account("0000000001", "Everyday", "90.00");
        Account to = account("0000000002", "Savings", "10.00");
        ReflectionTestUtils.setField(from, "id", 7L);
        ReflectionTestUtils.setField(to, "id", 8L);
        Transfer transfer = new Transfer(from, to, new BigDecimal("10.00"), "Savings top up");
        ReflectionTestUtils.setField(transfer, "id", 11L);
        Transaction debit = new Transaction(from, transfer, TransactionType.DEBIT,
                new BigDecimal("10.00"), new BigDecimal("90.00"), "Savings top up");
        Transaction openingCredit = new Transaction(from, null, TransactionType.CREDIT,
                new BigDecimal("100.00"), new BigDecimal("100.00"), "Opening balance");
        when(accountRepository.findByIdAndCustomer_Auth0Subject(7L, SUBJECT)).thenReturn(Optional.of(from));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(7L))
                .thenReturn(List.of(debit, openingCredit));

        List<TransactionResponse> responses = accountService.getTransactionsByAccountId(7L, SUBJECT);

        assertEquals(2, responses.size());
        assertEquals(TransactionType.DEBIT, responses.get(0).type());
        assertEquals(new BigDecimal("90.00"), responses.get(0).balanceAfter());
        assertEquals(11L, responses.get(0).transfer().id());
        assertEquals("0000000001", responses.get(0).transfer().fromAccount().accountNumber());
        assertEquals("0000000002", responses.get(0).transfer().toAccount().accountNumber());
        assertEquals(TransactionType.CREDIT, responses.get(1).type());
        assertNull(responses.get(1).transfer());
    }

    private Account account(String number, String name, String balance) {
        return new Account(number, name, new BigDecimal(balance), AccountType.CHECKING,
                new Customer(SUBJECT, "alex@example.com"));
    }
}
