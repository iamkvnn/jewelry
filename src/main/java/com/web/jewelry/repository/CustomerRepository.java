package com.web.jewelry.repository;

import com.web.jewelry.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByBackupToken(String backupToken);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Page<Customer> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.joinAt BETWEEN :from AND :to")
    Long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT c FROM Customer c WHERE c.isSubscribedForNews = true")
    List<Customer> findAllByIsSubscribedForNews();

    Optional<Customer> findByDeleteAccountToken(String token);
}
