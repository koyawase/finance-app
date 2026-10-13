package com.example.backend.account.service;

import java.util.List;

import com.example.backend.account.dto.AccountResponse;
import com.example.backend.account.dto.AccountSummary;
import com.example.backend.account.entity.Account;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.customer.service.CustomerService;
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
    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, CustomerService customerService,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
        this.transactionRepository = transactionRepository;
    }

    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        customerService.getCustomerById(customerId);
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::toAccountResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return transactionRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId).stream()
                .map(this::toTransactionResponse)
                .toList();
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
                toAccountSummary(transfer.getToAccount()));
    }

    private AccountSummary toAccountSummary(Account account) {
        return new AccountSummary(account.getId(), account.getAccountName(), account.getAccountNumber());
    }
}
