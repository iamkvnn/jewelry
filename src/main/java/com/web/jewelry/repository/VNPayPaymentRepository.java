package com.web.jewelry.repository;

import com.web.jewelry.model.VNPayPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VNPayPaymentRepository extends JpaRepository<VNPayPayment, Long> {
    Optional<VNPayPayment> findByOrderId(String id);
}
