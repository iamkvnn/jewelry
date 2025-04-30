package com.web.jewelry.repository;

import com.web.jewelry.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT SUM(o.totalPrice) FROM orders o WHERE o.orderDate BETWEEN :from AND :to")
    Long sumTotalPriceByOrderDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(o) FROM orders o WHERE o.orderDate BETWEEN :from AND :to")
    Long countByOrderDateBetween(LocalDateTime from, LocalDateTime to);

    List<Order> findByOrderDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(o) FROM orders o WHERE o.orderDate BETWEEN :from AND :to AND o.status = 'RETURNED'")
    Long countByOrderDateBetweenAndReturned(LocalDateTime from, LocalDateTime to);
}
