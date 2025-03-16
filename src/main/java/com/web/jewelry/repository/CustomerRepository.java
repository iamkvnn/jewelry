package com.web.jewelry.repository;

import com.web.jewelry.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Customer> findByUsername(String username);
}
