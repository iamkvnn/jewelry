package com.web.jewelry.repository;

import com.web.jewelry.model.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    Optional<ReturnItem> findByOrderItemId(Long itemId);
}
