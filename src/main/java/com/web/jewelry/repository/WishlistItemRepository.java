package com.web.jewelry.repository;

import com.web.jewelry.model.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Page<WishlistItem> findAllByCustomerId(Long customerId, Pageable pageable);
    Optional<WishlistItem> findByCustomerIdAndProductId(Long customerId, Long productId);
    @Modifying
    @Query("delete from WishlistItem w where w.product.id = ?1 and w.customer.id = ?2")
    void deleteByProductIdAndCustomerId(Long id, Long customerId);
}
