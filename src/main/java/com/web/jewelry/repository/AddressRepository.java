package com.web.jewelry.repository;

import com.web.jewelry.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Page<Address> findAllByCustomerId(Long customerId, Pageable pageable);
}
