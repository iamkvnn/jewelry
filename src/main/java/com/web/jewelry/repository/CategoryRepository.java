package com.web.jewelry.repository;

import com.web.jewelry.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByParentId(Long parentId);
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
