package com.web.jewelry.repository;

import com.web.jewelry.model.BannerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BannerImageRepository extends JpaRepository<BannerImage, Long> {
    List<BannerImage> findAllByPosition(String position);
}
