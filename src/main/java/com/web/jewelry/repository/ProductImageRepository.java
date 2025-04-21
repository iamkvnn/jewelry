package com.web.jewelry.repository;

import com.web.jewelry.model.Image;
import com.web.jewelry.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    ProductImage findByProductId(Long productId);
}
