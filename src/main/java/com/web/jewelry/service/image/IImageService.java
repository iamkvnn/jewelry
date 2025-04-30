package com.web.jewelry.service.image;

import com.web.jewelry.dto.response.ImageResponse;
import com.web.jewelry.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long imageId);
    List<ImageResponse> addImage(Long productId, List<MultipartFile> files);
    ImageResponse updateImage(Long imageId, MultipartFile file);
    void deleteImage(Long imageId);
    <T> ImageResponse convertToResponse(T image);
}
