package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.ImageResponse;
import com.web.jewelry.model.Image;
import com.web.jewelry.service.image.IImageService;
import com.web.jewelry.service.image.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/images")
public class ProductImageController {
    private final IImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getImageById(@PathVariable Long id) {
        Image image = imageService.getImageById(id);
        ImageResponse imageResponse = imageService.convertToResponse(image);
        return ResponseEntity.ok(new ApiResponse("200", "Success", imageResponse));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadImage(@RequestParam Long productId, @RequestParam List<MultipartFile> files) {
        List<ImageResponse> images = imageService.addImage(productId, files);
        return ResponseEntity.ok(new ApiResponse("200", "Success", images));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
