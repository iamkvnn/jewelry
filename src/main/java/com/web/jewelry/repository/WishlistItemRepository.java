package com.web.jewelry.repository;

import com.web.jewelry.model.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Page<WishlistItem> findAllByCustomerId(Long customerId, Pageable pageable);
    Optional<WishlistItem> findByCustomerIdAndProductId(Long customerId, Long productId);
}
