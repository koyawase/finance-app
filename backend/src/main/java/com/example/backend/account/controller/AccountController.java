package com.example.backend.account.controller;

import java.util.List;

import com.example.backend.account.dto.AccountResponse;
import com.example.backend.account.dto.AccountCreateRequest;
import com.example.backend.account.service.AccountService;
import com.example.backend.transaction.dto.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccountsForAuthenticatedCustomer(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountService.getAccountsForSubject(jwt.getSubject()));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccountId(
            @PathVariable Long accountId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountService.getTransactionsByAccountId(accountId, jwt.getSubject()));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccountForSubject(jwt.getSubject(), request));
    }
}
