package com.example.backend.transfer.service;

import java.math.BigDecimal;

import com.example.backend.account.dto.AccountSummary;
import com.example.backend.account.entity.Account;
import com.example.backend.account.entity.AccountStatus;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.entity.TransactionType;
import com.example.backend.transaction.repository.TransactionRepository;
import com.example.backend.transfer.dto.TransferRequest;
import com.example.backend.transfer.dto.TransferResponse;
import com.example.backend.transfer.entity.Transfer;
import com.example.backend.transfer.repository.TransferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository,
                           TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        String fromAccountNumber = request.fromAccountNumber();
        String toAccountNumber = request.toAccountNumber();
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to accounts must differ");
        }

        BigDecimal amount = request.amount();
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount can have at most two decimal places");
        }

        Account fromAccount = findAccount(fromAccountNumber);
        Account toAccount = findAccount(toAccountNumber);

        if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Both accounts must be active");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }

        fromAccount.debit(amount);
        toAccount.credit(amount);

        Transfer transfer = transferRepository.save(
                new Transfer(fromAccount, toAccount, amount, request.reference()));
        transactionRepository.save(new Transaction(fromAccount, transfer, TransactionType.DEBIT,
                amount, fromAccount.getBalance(), request.reference()));
        transactionRepository.save(new Transaction(toAccount, transfer, TransactionType.CREDIT,
                amount, toAccount.getBalance(), request.reference()));

        return toTransferResponse(transfer);
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found: " + accountNumber));
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
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
