package com.web.jewelry.repository;

import com.web.jewelry.model.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByProductIdAndAttributeId(Long productId, Long attributeId);
    @Modifying
    @Query("delete from AttributeValue f where f.product.id = ?1 and f.attribute.id = ?2")
    void deleteByProductIdAndAttributeId(Long productId, Long attributeId);
    boolean existsByProductIdAndAttributeId(Long productId, Long attributeId);
}
