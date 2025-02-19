package com.web.jewelry.service.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.jewelry.dto.response.ImageResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Image;
import com.web.jewelry.model.Product;
import com.web.jewelry.repository.ImageRepository;
import com.web.jewelry.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageService implements IImageService{
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary;

    @Override
    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    }

    @Override
    public List<ImageResponse> addImage(Long productId, List<MultipartFile> files) {
        Product product = productService.getProductById(productId);
        List<ImageResponse> imageResponses = new ArrayList<>();
        files.forEach(file -> {
            String fileName = file.getOriginalFilename();
            assert fileName != null;
            String publicId = generatePublicId(fileName);
            Image savedImage = imageRepository.save(Image.builder()
                    .name(publicId)
                    .url(uploadImage(file, publicId))
                    .product(product)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            imageResponses.add(convertToImageResponse(savedImage));
        });
        return imageResponses;
    }

    private String uploadImage(MultipartFile file, String publicId) {
        try {
            cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("public_id", publicId));
            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image");
        }
    }

    private String generatePublicId(String fileName) {
        return StringUtils.join(fileName.split("\\.")[0], "_", LocalDateTime.now());
    }

    private String getFileExtension(String fileName) {
        return fileName.split("\\.")[1];
    }

    @Override
    public ImageResponse updateImage(Long imageId, MultipartFile file) {
        Image image = getImageById(imageId);
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        String publicId = generatePublicId(fileName);
        deleteFromCloudinary(image.getName());
        image.setName(publicId);
        image.setUrl(uploadImage(file, publicId));
        image.setUpdatedAt(LocalDateTime.now());
        Image updatedImage = imageRepository.save(image);
        return convertToImageResponse(updatedImage);
    }

    @Override
    public void deleteImage(Long imageId) {
        imageRepository.findById(imageId).ifPresentOrElse((image) -> {
            deleteFromCloudinary(image.getName());
            imageRepository.delete(image);
        }, () -> {
            throw new ResourceNotFoundException("Image not found");
        });
    }

    private void deleteFromCloudinary(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image");
        }
    }

    @Override
    public ImageResponse convertToImageResponse(Image image) {
        return modelMapper.map(image, ImageResponse.class);
    }
}
