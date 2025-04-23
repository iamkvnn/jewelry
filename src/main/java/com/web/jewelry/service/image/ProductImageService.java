package com.web.jewelry.service.image;

import com.web.jewelry.dto.response.ImageResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Image;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.ProductImage;
import com.web.jewelry.repository.ProductImageRepository;
import com.web.jewelry.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductImageService implements IImageService{
    private final ProductImageRepository productImageRepository;
    private final IProductService productService;
    private final ModelMapper modelMapper;
    private final ImageSaverContext imageSaverContext;
    private final CloudinaryImageSaverStrategy cloudinary;

    @Override
    public ProductImage getImageById(Long imageId) {
        return productImageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    }

    @Override
    public List<ImageResponse> addImage(Long productId, List<MultipartFile> files) {
        imageSaverContext.setImageSaverStrategy(cloudinary);
        Product product = productService.getProductById(productId);
        List<ImageResponse> imageResponses = new ArrayList<>();
        files.forEach(file -> {
            Map<String, String> imageData = imageSaverContext.saveImage(file);
            ProductImage savedImage = productImageRepository.save(ProductImage.builder()
                    .name(imageData.get("publicId"))
                    .url(imageData.get("url"))
                    .product(product)
                    .createdAt(LocalDateTime.now())
                    .build());
            imageResponses.add(convertToResponse(savedImage));
        });
        return imageResponses;
    }

    @Override
    public ImageResponse updateImage(Long imageId, MultipartFile file) {
        imageSaverContext.setImageSaverStrategy(cloudinary);
        ProductImage image = getImageById(imageId);
        String publicId = image.getName();
        imageSaverContext.deleteImage(publicId);
        Map<String, String> imageData = imageSaverContext.saveImage(file);
        image.setName(imageData.get("publicId"));
        image.setUrl(imageData.get("url"));
        image.setUpdatedAt(LocalDateTime.now());
        Image updatedImage = productImageRepository.save(image);
        return convertToResponse(updatedImage);
    }

    @Override
    public void deleteImage(Long imageId) {
        imageSaverContext.setImageSaverStrategy(cloudinary);
        productImageRepository.findById(imageId).ifPresentOrElse((image) -> {
            if (image.getId() > 4294) {
                imageSaverContext.deleteImage(image.getName());
                log.info("deleted");
            }
            productImageRepository.delete(image);
        }, () -> {
            throw new ResourceNotFoundException("Image not found");
        });
    }

    @Override
    public <T> ImageResponse convertToResponse(T image) {
        return modelMapper.map(image, ImageResponse.class);
    }
}
