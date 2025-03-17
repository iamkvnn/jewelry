package com.web.jewelry.repository;

import com.web.jewelry.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductSizeId(Long cartId, Long productSizeId);
    Optional<CartItem> findByIdAndCartId(Long cartItemId, Long cartId);
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cart.id = ?1 AND c.productSize.id IN ?2")
    void deleteAllByCartIdAndProductSizeIdIn(Long cartId, List<Long> productSizeIds);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.productSize.id = ?1")
    void deleteAllByByProductSizeId(Long productSizeId);
}
