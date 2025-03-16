package com.web.jewelry.repository;

import com.web.jewelry.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    boolean existsByEmail(String email);
    Manager findByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Manager> findByUsername(String username);
}
