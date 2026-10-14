package com.example.backend.account.repository;

import java.util.List;
import java.util.Optional;

import com.example.backend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomer_Auth0Subject(String subject);
    Optional<Account> findByIdAndCustomer_Auth0Subject(Long accountId, String subject);
    Optional<Account> findByAccountNumber(String accountNumber);
}
