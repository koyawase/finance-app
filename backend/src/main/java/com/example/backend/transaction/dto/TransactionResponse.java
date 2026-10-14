package com.example.backend.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.backend.transaction.entity.TransactionType;
import com.example.backend.transfer.dto.TransferResponse;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String reference,
        Instant createdAt,
        TransferResponse transfer
) {}
