package com.web.jewelry.repository;

import com.web.jewelry.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByBackupToken(String backupToken);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Page<Staff> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
}
