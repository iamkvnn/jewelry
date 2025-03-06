package com.web.jewelry.repository;

import com.web.jewelry.model.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Attribute findByName(String name);
}
