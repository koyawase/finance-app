package com.example.backend.customer.service;

import com.example.backend.customer.dto.CustomerResponse;
import com.example.backend.customer.entity.Customer;
import com.example.backend.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String SUBJECT = "auth0|customer-1";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getCustomerReturnsCustomerLinkedToSubject() {
        Customer customer = new Customer(SUBJECT, "alex@example.com");
        customer.setName("Alex");
        customer.setPhoneNumber("0212345678");
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerForAuth0User(SUBJECT);

        assertEquals("Alex", response.name());
        assertEquals("alex@example.com", response.email());
        assertEquals("0212345678", response.phoneNumber());
    }

    @Test
    void getCustomerReturnsNotFoundWhenNoCustomerIsLinked() {
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerService.getCustomerForAuth0User(SUBJECT));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No customer is linked to this Auth0 user yet", exception.getReason());
    }

    @Test
    void getOrCreateCreatesCustomerForNewSubject() {
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.getOrCreateForAuth0User(SUBJECT, "alex@example.com");

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals(SUBJECT, customerCaptor.getValue().getAuth0Subject());
        assertEquals("alex@example.com", response.email());
        assertNull(response.name());
        assertNull(response.phoneNumber());
    }

    @Test
    void getOrCreateKeepsExistingCustomerAndEmail() {
        Customer existing = new Customer(SUBJECT, "original@example.com");
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.of(existing));

        CustomerResponse response = customerService.getOrCreateForAuth0User(SUBJECT, "new@example.com");

        assertEquals("original@example.com", response.email());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateDetailsChangesLinkedCustomer() {
        Customer customer = new Customer(SUBJECT, "alex@example.com");
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.updateDetailsForAuth0User(SUBJECT, "Alex", "0212345678");

        assertEquals("Alex", customer.getName());
        assertEquals("0212345678", customer.getPhoneNumber());
        assertEquals("Alex", response.name());
        assertEquals("0212345678", response.phoneNumber());
    }

    @Test
    void updateDetailsReturnsNotFoundWhenNoCustomerIsLinked() {
        when(customerRepository.findByAuth0Subject(SUBJECT)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerService.updateDetailsForAuth0User(SUBJECT, "Alex", "0212345678"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
