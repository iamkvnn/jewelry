package com.web.jewelry.repository;

import com.web.jewelry.model.MomoPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MomoPaymentRepository extends JpaRepository<MomoPayment, Long> {
    Optional<MomoPayment> findByOrderId(String orderId);
}
