package com.web.jewelry.service.image;

import org.springframework.web.multipart.MultipartFile;

public interface IImageSaverStrategy {
    String saveImage(MultipartFile file, String publicId);
    void deleteImage(String publicId);
}
