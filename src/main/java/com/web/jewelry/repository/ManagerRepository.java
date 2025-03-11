package com.web.jewelry.repository;

import com.web.jewelry.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    boolean existsByEmail(String email);
    Manager findByEmail(String email);
    boolean existsByPhone(String phone);
}
