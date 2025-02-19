package com.web.jewelry.repository;

import com.web.jewelry.model.FeatureValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FeatureValueRepository extends JpaRepository<FeatureValue, Long> {
    Optional<FeatureValue> findByProductIdAndFeatureId(Long productId, Long featureId);
    @Modifying
    @Query("delete from FeatureValue f where f.product.id = ?1 and f.feature.id = ?2")
    void deleteByProductIdAndFeatureId(Long productId, Long featureId);
}
