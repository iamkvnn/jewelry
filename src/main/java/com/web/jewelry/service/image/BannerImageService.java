package com.web.jewelry.service.image;

import com.web.jewelry.dto.response.BannerImageResponse;
import com.web.jewelry.dto.response.ImageResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.BannerImage;
import com.web.jewelry.repository.BannerImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class BannerImageService implements IImageService {
    private final ImageSaverContext imageSaverContext;
    private final BannerImageRepository bannerImageRepository;
    private final ModelMapper modelMapper;
    private final LocalImageSaverStrategy local;

    public List<BannerImage> getAllImages() {
        return bannerImageRepository.findAll();
    }

    @Override
    public BannerImage getImageById(Long imageId) {
        return bannerImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    }

    @Override
    public List<ImageResponse> addImage(Long productId, List<MultipartFile> files) {
        return null;
    }

    @Override
    public BannerImageResponse updateImage(Long imageId, MultipartFile file) {
        imageSaverContext.setImageSaverStrategy(local);
        BannerImage existingImage = bannerImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        imageSaverContext.deleteImage(existingImage.getName());
        Map<String, String> imageData = imageSaverContext.saveImage(file);
        existingImage.setName(imageData.get("publicId"));
        existingImage.setUrl(imageData.get("url"));
        existingImage.setUpdatedAt(LocalDateTime.now());
        bannerImageRepository.save(existingImage);
        return convertToResponse(existingImage);
    }

    @Override
    public void deleteImage(Long imageId) {

    }

    @Override
    public <T> BannerImageResponse convertToResponse(T image) {
        return modelMapper.map(image, BannerImageResponse.class);
    }

    public List<BannerImageResponse> convertToResponses(List<BannerImage> images) {
        return images.stream()
                .map(this::convertToResponse)
                .toList();
    }
}
