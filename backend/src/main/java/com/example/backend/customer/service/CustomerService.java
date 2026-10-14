package com.example.backend.customer.service;

import com.example.backend.customer.dto.CustomerResponse;
import com.example.backend.customer.entity.Customer;
import com.example.backend.customer.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse getCustomerForAuth0User(String subject) {
        return customerRepository.findByAuth0Subject(subject)
                .map(this::toCustomerResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No customer is linked to this Auth0 user yet"));
    }

    @Transactional
    public CustomerResponse getOrCreateForAuth0User(String subject, String email) {
        Customer customer = customerRepository.findByAuth0Subject(subject)
                .orElseGet(() -> customerRepository.save(new Customer(subject, email)));
        return toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateDetailsForAuth0User(String subject, String name, String phoneNumber) {
        Customer customer = customerRepository.findByAuth0Subject(subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No customer is linked to this Auth0 user yet"));
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        return toCustomerResponse(customer);
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber());
    }

}
