package com.web.jewelry.repository;

import com.web.jewelry.model.SizeVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SizeVariantRepository extends JpaRepository<SizeVariant, Long> {
    List<SizeVariant> findByProductId(Long productId);
    Optional<SizeVariant> findBySizeAndProductId(String size, Long id);
    @Modifying
    @Query("delete from SizeVariant s where s.product.id = ?1 and s.size = ?2")
    void deleteByProductIdAndSize(Long productId, String size);
    boolean existsBySizeAndProductId(String size, Long id);
}
