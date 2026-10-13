package com.example.backend.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.backend.account.dto.AccountSummary;

public record TransferResponse(
        Long id,
        AccountSummary fromAccount,
        AccountSummary toAccount,
        BigDecimal amount,
        String reference,
        Instant createdAt
) {
}
