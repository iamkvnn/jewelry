package com.web.jewelry.service.image;

import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.ProofImage;
import com.web.jewelry.model.ReturnItem;
import com.web.jewelry.repository.ProofImageRepository;
import com.web.jewelry.repository.ReturnItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProofImageService {
    private final ImageSaverContext imageSaverContext;
    private final ReturnItemRepository returnItemRepository;
    private final ProofImageRepository proofImageRepository;
    private final CloudinaryImageSaverStrategy cloudinaryImageSaverStrategy;

    public void saveImage(Long itemId, List<MultipartFile> files) {
        imageSaverContext.setImageSaverStrategy(cloudinaryImageSaverStrategy);
        ReturnItem returnItem = returnItemRepository.findByOrderItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Return item not found"));
        files.forEach(file -> {
            Map<String, String> imageData = imageSaverContext.saveImage(file);
            String publicId = imageData.get("publicId");
            String url = imageData.get("url");
            proofImageRepository.save(ProofImage.builder()
                    .name(publicId)
                    .url(url)
                    .createdAt(LocalDateTime.now())
                    .returnItem(returnItem)
                    .build());
        });
    }
}
