package com.web.jewelry.repository;

import com.web.jewelry.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    boolean existsByName(String name);
    List<Collection> findByNameContaining(String name);
}
