package com.example.backend.account.dto;

import java.math.BigDecimal;

import com.example.backend.account.entity.AccountStatus;
import com.example.backend.account.entity.AccountType;

public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus status
) {
}
