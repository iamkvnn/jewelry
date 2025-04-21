package com.web.jewelry.service.image;

import org.springframework.web.multipart.MultipartFile;

public abstract class ImageSaverStrategy {
    public abstract String saveImage(MultipartFile file, String publicId);
    public abstract String generatePublicId(String fileName);
    public  abstract void deleteImage(String publicId);
}
