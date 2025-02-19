package com.web.jewelry.repository;

import com.web.jewelry.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByProductId(Long productId);
}
