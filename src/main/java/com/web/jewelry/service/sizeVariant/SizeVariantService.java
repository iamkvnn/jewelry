package com.web.jewelry.service.sizeVariant;

import com.web.jewelry.dto.request.SizeVariantRequest;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.SizeVariant;
import com.web.jewelry.repository.ProductRepository;
import com.web.jewelry.repository.SizeVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SizeVariantService implements ISizeVariantService {
    private final SizeVariantRepository sizeVariantRepository;
    private final ProductRepository productRepository;

    @Override
    public List<SizeVariant> addSizeVariant(Product product, List<SizeVariantRequest> request) {
        return request.stream().map(sizeVariantRequest -> {
            if (sizeVariantRepository.existsBySizeAndProductId(sizeVariantRequest.getSize(), product.getId())) {
                throw new AlreadyExistException("Size variant " + sizeVariantRequest.getSize() + " duplicated for this product");
            }
            return sizeVariantRepository.save(SizeVariant.builder()
                    .product(product)
                    .size(sizeVariantRequest.getSize())
                    .stock(sizeVariantRequest.getStock())
                    .price(sizeVariantRequest.getPrice())
                    .discountPrice(sizeVariantRequest.getDiscountPrice())
                    .discountRate(sizeVariantRequest.getDiscountRate())
                    .createdAt(LocalDateTime.now())
                    .build());
        }).distinct().toList();
    }

    @Override
    public List<SizeVariant> updateSizeVariant(Product product, List<SizeVariantRequest> request) {
        product.getSizeVariants().stream()
                .filter(sizeVariant -> request.stream()
                        .noneMatch(sizeVariantRequest -> sizeVariantRequest.getSize().equals(sizeVariant.getSize())))
                .forEach(sizeVariant -> sizeVariantRepository.deleteByProductIdAndSize(product.getId(), sizeVariant.getSize()));
        return request.stream()
                .map(sizeVariantRequest -> {
                    SizeVariant sizeVariant = sizeVariantRepository.findBySizeAndProductId(sizeVariantRequest.getSize(), product.getId())
                            .orElseGet(() -> SizeVariant.builder()
                                    .product(product)
                                    .createdAt(LocalDateTime.now())
                                    .build());
                    sizeVariant.setSize(sizeVariantRequest.getSize());
                    sizeVariant.setStock(sizeVariantRequest.getStock());
                    sizeVariant.setPrice(sizeVariantRequest.getPrice());
                    sizeVariant.setDiscountPrice(sizeVariantRequest.getDiscountPrice());
                    sizeVariant.setDiscountRate(sizeVariantRequest.getDiscountRate());
                    return sizeVariantRepository.save(sizeVariant);
                }).distinct().toList();
    }
}
