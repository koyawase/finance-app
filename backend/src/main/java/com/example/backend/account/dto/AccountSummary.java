package com.example.backend.account.dto;

public record AccountSummary(
    Long id,
    String accountName,
    String accountNumber
) {}
