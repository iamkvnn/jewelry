package com.web.jewelry.repository;

import com.web.jewelry.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductSizeId(Long cartId, Long productSizeId);
    List<CartItem> findAllByCartIdAndIsChecked(Long cartId, boolean checked);
}
