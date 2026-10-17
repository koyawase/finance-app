package com.example.backend;

import com.example.backend.account.entity.Account;
import com.example.backend.account.entity.AccountStatus;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.entity.TransactionType;
import com.example.backend.transaction.repository.TransactionRepository;
import com.example.backend.transfer.dto.TransferRequest;
import com.example.backend.transfer.repository.TransferRepository;
import com.example.backend.transfer.service.TransferService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String MESSI_SUBJECT = "auth0|6acf50141830a1fdcf3cd31c";
    private static final String MESSI_ACCOUNT = "0000000001";
    private static final String RONALDO_ACCOUNT = "0000000003";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransferService transferService;

    @Autowired
    private EntityManager entityManager;

    // Avoid Auth0 issuer discovery while retaining the real security filter chain.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCustomerSeesOnlyTheirAccounts() throws Exception {
        mockMvc.perform(get("/accounts")
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].accountNumber",
                        containsInAnyOrder("0000000001", "0000000002")));
    }

    @Test
    void anotherCustomersTransactionHistoryIsHidden() throws Exception {
        Long ronaldosAccountId = accountRepository.findByAccountNumber(RONALDO_ACCOUNT).orElseThrow().getId();

        mockMvc.perform(get("/accounts/{accountId}/transactions", ronaldosAccountId)
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void invalidAccountRequestIsRejectedBeforeSaving() throws Exception {
        long accountCount = accountRepository.count();

        mockMvc.perform(post("/accounts")
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountName":"Travel fund","accountType":"SAVINGS","balance":-1.00}
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(accountCount, accountRepository.count());
    }

    @Test
    void invalidTransferAmountIsRejectedBeforeSaving() throws Exception {
        long transferCount = transferRepository.count();
        long transactionCount = transactionRepository.count();

        mockMvc.perform(post("/transfers")
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountNumber":"0000000001","toAccountNumber":"0000000003",
                                 "amount":0.00,"reference":"Invalid amount"}
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(transferCount, transferRepository.count());
        assertEquals(transactionCount, transactionRepository.count());
    }

    @Test
    @Transactional
    void accountCreationPersistsAnActiveAccountForTheAuthenticatedCustomer() throws Exception {
        long accountCount = accountRepository.count();

        mockMvc.perform(post("/accounts")
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountName":"  Travel fund  ","accountType":"SAVINGS","balance":250.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.accountName").value("Travel fund"))
                .andExpect(jsonPath("$.accountNumber").isString())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        entityManager.flush();
        entityManager.clear();
        assertEquals(accountCount + 1, accountRepository.count());
        Account created = accountRepository.findByCustomer_Auth0Subject(MESSI_SUBJECT).stream()
                .filter(account -> account.getAccountName().equals("Travel fund"))
                .findFirst().orElseThrow();
        assertTrue(created.getAccountNumber().matches("[0-9]{10}"));
        assertEquals(new BigDecimal("250.00"), created.getBalance());
        assertEquals(AccountStatus.ACTIVE, created.getStatus());
        assertEquals(MESSI_SUBJECT, created.getCustomer().getAuth0Subject());
    }

    @Test
    @Transactional
    void transferPersistsBalancesAndBothTransactionRecords() throws Exception {
        long transferCount = transferRepository.count();
        long transactionCount = transactionRepository.count();

        mockMvc.perform(post("/transfers")
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountNumber":"0000000001","toAccountNumber":"0000000003",
                                 "amount":10.00,"reference":"Integration payment"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromAccount.accountNumber").value(MESSI_ACCOUNT))
                .andExpect(jsonPath("$.toAccount.accountNumber").value(RONALDO_ACCOUNT));

        entityManager.flush();
        entityManager.clear();
        assertEquals(transferCount + 1, transferRepository.count());
        assertEquals(transactionCount + 2, transactionRepository.count());
        Account source = accountRepository.findByAccountNumber(MESSI_ACCOUNT).orElseThrow();
        Account destination = accountRepository.findByAccountNumber(RONALDO_ACCOUNT).orElseThrow();
        assertEquals(new BigDecimal("14990.00"), source.getBalance());
        assertEquals(new BigDecimal("25010.00"), destination.getBalance());
        assertTransaction(source.getId(), TransactionType.DEBIT, "14990.00");
        assertTransaction(destination.getId(), TransactionType.CREDIT, "25010.00");
    }

    @Test
    void rejectedTransferDoesNotChangeBalancesOrHistory() throws Exception {
        long transferCount = transferRepository.count();
        long transactionCount = transactionRepository.count();

        mockMvc.perform(post("/transfers")
                        .with(jwt().jwt(token -> token.subject(MESSI_SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountNumber":"0000000001","toAccountNumber":"0000000003",
                                 "amount":20000.00,"reference":"Too much"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        assertEquals(transferCount, transferRepository.count());
        assertEquals(transactionCount, transactionRepository.count());
        assertEquals(new BigDecimal("15000.00"),
                accountRepository.findByAccountNumber(MESSI_ACCOUNT).orElseThrow().getBalance());
        assertEquals(new BigDecimal("25000.00"),
                accountRepository.findByAccountNumber(RONALDO_ACCOUNT).orElseThrow().getBalance());
    }

    @Test
    void transferRollsBackWhenSavingFails() {
        long transferCount = transferRepository.count();
        long transactionCount = transactionRepository.count();
        TransferRequest request = new TransferRequest(MESSI_ACCOUNT, RONALDO_ACCOUNT,
                new BigDecimal("10.00"), "reference-longer-than-twenty-characters");

        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> transferService.transfer(request, MESSI_SUBJECT));
        assertTrue(isConstraintFailure(failure),
                () -> "Expected a persistence constraint failure, got " + failure.getClass().getName());

        assertEquals(transferCount, transferRepository.count());
        assertEquals(transactionCount, transactionRepository.count());
        assertEquals(new BigDecimal("15000.00"),
                accountRepository.findByAccountNumber(MESSI_ACCOUNT).orElseThrow().getBalance());
        assertEquals(new BigDecimal("25000.00"),
                accountRepository.findByAccountNumber(RONALDO_ACCOUNT).orElseThrow().getBalance());
    }

    private void assertTransaction(Long accountId, TransactionType type, String balanceAfter) {
        List<Transaction> matches = transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId)
                .stream()
                .filter(transaction -> "Integration payment".equals(transaction.getReference()))
                .toList();
        assertEquals(1, matches.size());
        assertEquals(type, matches.get(0).getType());
        assertEquals(new BigDecimal("10.00"), matches.get(0).getAmount());
        assertEquals(new BigDecimal(balanceAfter), matches.get(0).getBalanceAfter());
        assertTrue(matches.get(0).getTransfer() != null);
    }

    private boolean isConstraintFailure(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException || cause instanceof DataIntegrityViolationException) {
                return true;
            }
        }
        return false;
    }
}
