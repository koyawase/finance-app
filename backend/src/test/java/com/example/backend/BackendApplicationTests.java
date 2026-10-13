package com.example.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.account.entity.Account;
import com.example.backend.account.entity.AccountStatus;
import com.example.backend.account.entity.AccountType;

import jakarta.persistence.EntityManager;

@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private EntityManager entityManager;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void seededAccountsBelongToExistingCustomers() {
		List<Account> accounts = entityManager.createQuery(
				"select a from Account a join fetch a.customer order by a.accountNumber", Account.class)
				.getResultList();

		assertThat(accounts).hasSize(4);
		assertThat(accounts).extracting(account -> account.getCustomer().getEmail())
				.containsExactly("leothegoat@test.com", "leothegoat@test.com",
						"cristiano@test.com", "cristiano@test.com");
		assertThat(accounts).extracting(Account::getAccountType)
				.containsExactly(AccountType.CHECKING, AccountType.SAVINGS,
						AccountType.CHECKING, AccountType.SAVINGS);
		assertThat(accounts).extracting(Account::getBalance)
				.containsExactly(new BigDecimal("1500.00"), new BigDecimal("5000.00"),
						new BigDecimal("2500.00"), new BigDecimal("10000.00"));
		assertThat(accounts).allSatisfy(account -> {
			assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
			assertThat(account.getCreatedAt()).isNotNull();
		});
	}

}
