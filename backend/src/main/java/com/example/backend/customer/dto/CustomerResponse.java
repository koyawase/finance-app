package com.example.backend.customer.dto;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phoneNumber
) {}
