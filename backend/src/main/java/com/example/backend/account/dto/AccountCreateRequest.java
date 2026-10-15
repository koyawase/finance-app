package com.example.backend.account.dto;

import java.math.BigDecimal;

import com.example.backend.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountCreateRequest(
        @NotBlank @Size(max = 255) String accountName,
        @NotNull AccountType accountType,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal balance
) {}
