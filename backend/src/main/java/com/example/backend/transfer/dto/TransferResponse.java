package com.example.backend.transfer.dto;

import com.example.backend.account.dto.AccountSummary;

public record TransferResponse(
        Long id,
        AccountSummary fromAccount,
        AccountSummary toAccount
) {
}
