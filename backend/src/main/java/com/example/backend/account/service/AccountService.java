package com.example.backend.account.service;

import java.util.List;

import com.example.backend.account.dto.AccountResponse;
import com.example.backend.account.entity.Account;
import com.example.backend.account.repository.AccountRepository;
import com.example.backend.customer.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerService customerService;

    public AccountService(AccountRepository accountRepository, CustomerService customerService) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
    }

    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        customerService.getCustomerById(customerId);
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::toAccountResponse)
                .toList();
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus());
    }
}
