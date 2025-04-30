package com.web.jewelry.service.image;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Setter
@RequiredArgsConstructor
@Component
public class ImageSaverContext {
    private IImageSaverStrategy imageSaverStrategy;

    public Map<String, String> saveImage(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        String publicId = generatePublicId(fileName);
        String url = imageSaverStrategy.saveImage(file, publicId);
        return Map.of("publicId", publicId, "url", url);
    }

    public void deleteImage(String publicId) {
        imageSaverStrategy.deleteImage(publicId);
    }

    private String generatePublicId(String fileName) {
        return StringUtils.join(fileName.split("\\.")[0], "-", UUID.randomUUID().toString());
    }
}
