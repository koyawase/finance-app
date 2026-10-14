package com.example.backend.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerDetailsRequest(
        @NotBlank String name,
        @NotBlank String phoneNumber
) {}
