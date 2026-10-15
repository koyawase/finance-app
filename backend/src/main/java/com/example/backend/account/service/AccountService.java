package com.example.backend.account.service;

import java.util.List;
import java.security.SecureRandom;

import com.example.backend.account.dto.AccountResponse;
import com.example.backend.account.dto.AccountSummary;
import com.example.backend.account.dto.AccountCreateRequest;
import com.example.backend.account.entity.Account;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.customer.entity.Customer;
import com.example.backend.customer.repository.CustomerRepository;
import com.example.backend.transaction.dto.TransactionResponse;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.repository.TransactionRepository;
import com.example.backend.transfer.dto.TransferResponse;
import com.example.backend.transfer.entity.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    public List<AccountResponse> getAccountsForSubject(String subject) {
        return accountRepository.findByCustomer_Auth0Subject(subject).stream()
                .map(this::toAccountResponse)
                .toList();
    }

    public List<TransactionResponse> getTransactionsByAccountId(Long accountId, String subject) {
        if (accountRepository.findByIdAndCustomer_Auth0Subject(accountId, subject).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public AccountResponse createAccountForSubject(String subject, AccountCreateRequest request) {
        Customer customer = customerRepository.findByAuth0Subject(subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No customer is linked to this Auth0 user yet"));
        String accountNumber;
        do {
            accountNumber = String.format("%010d", secureRandom.nextInt(1_000_000_000));
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        Account account = accountRepository.save(new Account(
                accountNumber,
                request.accountName().trim(),
                request.balance(),
                request.accountType(),
                customer));
        return toAccountResponse(account);
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus());
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getReference(),
                transaction.getCreatedAt(),
                toTransferResponse(transaction.getTransfer()));
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        if (transfer == null) {
            return null;
        }
        return new TransferResponse(
                transfer.getId(),
                toAccountSummary(transfer.getFromAccount()),
                toAccountSummary(transfer.getToAccount()),
                transfer.getAmount(),
                transfer.getReference(),
                transfer.getCreatedAt());
    }

    private AccountSummary toAccountSummary(Account account) {
        return new AccountSummary(account.getId(), account.getAccountName(), account.getAccountNumber());
    }
}
