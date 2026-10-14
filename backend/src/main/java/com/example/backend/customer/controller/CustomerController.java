package com.example.backend.customer.controller;

import com.example.backend.customer.dto.CustomerResponse;
import com.example.backend.customer.dto.CustomerCreateRequest;
import com.example.backend.customer.dto.CustomerDetailsRequest;
import com.example.backend.customer.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<CustomerResponse> getCustomer(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerService.getCustomerForAuth0User(jwt.getSubject()));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.ok(customerService.getOrCreateForAuth0User(
                jwt.getSubject(), request.email().trim().toLowerCase()));
    }

    @PatchMapping
    public ResponseEntity<CustomerResponse> updateCustomerDetails(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CustomerDetailsRequest request) {
        return ResponseEntity.ok(customerService.updateDetailsForAuth0User(
                jwt.getSubject(), request.name().trim(), request.phoneNumber().trim()));
    }
}
