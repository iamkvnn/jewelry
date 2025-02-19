package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.ImageResponse;
import com.web.jewelry.model.Image;
import com.web.jewelry.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/images")
public class ImageController {
    private final IImageService imageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getImageById(@PathVariable Long id) {
        Image image = imageService.getImageById(id);
        ImageResponse imageResponse = imageService.convertToImageResponse(image);
        return ResponseEntity.ok(new ApiResponse("200", "Success", imageResponse));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadImage(@RequestParam Long productId, @RequestParam List<MultipartFile> files) {
        List<ImageResponse> images = imageService.addImage(productId, files);
        return ResponseEntity.ok(new ApiResponse("200", "Success", images));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateImage(@PathVariable Long id, @RequestParam MultipartFile file) {
        ImageResponse imageResponse = imageService.updateImage(id, file);
        return ResponseEntity.ok(new ApiResponse("200", "Success", imageResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
