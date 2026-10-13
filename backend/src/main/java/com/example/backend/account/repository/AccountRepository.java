package com.example.backend.account.repository;

import java.util.List;
import java.util.Optional;

import com.example.backend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerId(Long customerId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
