package com.web.jewelry.repository;

import com.web.jewelry.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Page<Address> findAllByCustomerId(Long customerId, Pageable pageable);
    boolean existsByCustomerId(Long customerId);
    Optional<Address> findByCustomerIdAndIsDefault(Long customerId, boolean isDefault);
    Optional<Address> findByIdAndCustomerId(Long id, Long customer_id);
}
