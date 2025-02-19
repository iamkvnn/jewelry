package com.web.jewelry.repository;

import com.web.jewelry.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
    Feature findByName(String name);
}
