package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.BannerImageResponse;
import com.web.jewelry.model.BannerImage;
import com.web.jewelry.service.image.BannerImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/banners")
public class BannerController {
    private final BannerImageService imageService;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllBanners() {
        List<BannerImage> banners = imageService.getAllImages();
        List<BannerImageResponse> responses = imageService.convertToResponses(banners);
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBannerById(@PathVariable Long id) {
        BannerImage banner = imageService.getImageById(id);
        BannerImageResponse response = imageService.convertToResponse(banner);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBanner(@PathVariable Long id, @RequestParam MultipartFile file) {
        BannerImageResponse response = imageService.updateImage(id, file);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }
}
