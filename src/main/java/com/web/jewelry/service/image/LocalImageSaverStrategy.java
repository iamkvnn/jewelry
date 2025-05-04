package com.web.jewelry.service.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalImageSaverStrategy implements IImageSaverStrategy{
    @Value("${app.image.upload-dir}")
    private String uploadDir;

    @Override
    public String saveImage(MultipartFile file, String publicId) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = file.getOriginalFilename();
            assert fileName != null;
            String extension = getFileExtension(fileName);
            fileName = publicId + "." + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteImage(String publicId) {
        File folder = new File(uploadDir);
        File[] matchingFiles = folder.listFiles((a, name) -> name.startsWith(publicId));

        if (matchingFiles != null && matchingFiles.length == 1) {
            boolean r = matchingFiles[0].delete();
            if (!r) {
                throw new RuntimeException("Failed to delete image");
            }
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.split("\\.")[1];
    }
}
