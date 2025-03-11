package com.web.jewelry.repository;

import com.web.jewelry.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
