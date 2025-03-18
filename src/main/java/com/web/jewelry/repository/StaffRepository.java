package com.web.jewelry.repository;

import com.web.jewelry.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Staff findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Staff> findByUsername(String username);
}
