package com.web.jewelry.repository;

import com.web.jewelry.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Staff findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
