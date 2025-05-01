package com.web.jewelry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.web.jewelry.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.productSizes ps " +
            "WHERE (:title IS NULL OR p.title LIKE '%' || :title || '%') " +
            "AND (:categories IS NULL OR p.category.id IN :categories OR p.category.parent.id IN :categories) " +
            "AND (:material IS NULL OR p.material = :material) " +
            "AND (:sizes IS NULL OR ps.size IN :sizes) " +
            "AND (:minPrice IS NULL OR ps.discountPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR ps.discountPrice <= :maxPrice)" +
            "AND p.status <> 'NOT_AVAILABLE' ")
    Page<Product> findByFilters(@Param("title") String title,
                                @Param("categories") List<Long> categories,
                                @Param("material") String material,
                                @Param("minPrice") Long minPrice,
                                @Param("maxPrice") Long maxPrice,
                                @Param("sizes") List<String> sizes,
                                Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p WHERE (p.category.id = :categoryId OR p.category.parent.id = :categoryId) " +
            "AND p.status <> 'NOT_AVAILABLE'")
    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.title = :title AND p.status <> 'NOT_AVAILABLE'")
    boolean existsByTitleAndAvailable(String title);
}
