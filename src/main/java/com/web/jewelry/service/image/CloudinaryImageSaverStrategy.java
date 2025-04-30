package com.web.jewelry.service.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@Component
public class CloudinaryImageSaverStrategy implements IImageSaverStrategy{
    private final Cloudinary cloudinary;

    @Override
    public String saveImage(MultipartFile file, String publicId) {
        try {
            cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("public_id", publicId));
            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image");
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image");
        }
    }
}
