package com.web.jewelry.repository;

import com.web.jewelry.model.BannerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BannerImageRepository extends JpaRepository<BannerImage, Long> {
    Optional<BannerImage> findByPosition(String pos);
}
