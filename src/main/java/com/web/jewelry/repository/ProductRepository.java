package com.web.jewelry.repository;

import com.web.jewelry.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.web.jewelry.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByTitleContaining(String title, Pageable pageable);
    boolean existsByTitle(String title);
}
