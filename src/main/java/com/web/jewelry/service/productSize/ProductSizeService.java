package com.web.jewelry.service.productSize;

import com.web.jewelry.dto.request.ProductSizeRequest;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.ProductSize;
import com.web.jewelry.repository.ProductSizeRepository;
import com.web.jewelry.service.observer.ProductSizeObservable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductSizeService implements IProductSizeService {
    private final ProductSizeRepository productSizeRepository;
    private final ProductSizeObservable productSizeObservable;

    @Override
    public ProductSize getProductSize(Long id) {
        return productSizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product size not found"));
    }

    @Override
    public List<ProductSize> addProductSize(Product product, List<ProductSizeRequest> request) {
        return request.stream().map(productSizeRequest -> productSizeRepository.save(ProductSize.builder()
                .product(product)
                .size(productSizeRequest.getSize())
                .stock(productSizeRequest.getStock())
                .price(productSizeRequest.getPrice())
                .sold(0L)
                .isDeleted(false)
                .discountPrice(productSizeRequest.getDiscountPrice())
                .discountRate(productSizeRequest.getDiscountRate())
                .build())).distinct().toList();
    }

    @Override
    public List<ProductSize> updateProductSize(Product product, List<ProductSizeRequest> request) {
        product.getProductSizes().stream()
                .filter(productSize -> request.stream()
                        .noneMatch(productSizeRequest -> productSizeRequest.getSize().equals(productSize.getSize())))
                .forEach(productSize -> {
                    productSize.setDeleted(true);
                    productSizeObservable.notifyObservers(productSize.getId());
                    productSizeRepository.save(productSize);
                });
        return request.stream()
                .map(productSizeRequest -> {
                    ProductSize productSize = productSizeRepository.findBySizeAndProductId(productSizeRequest.getSize(), product.getId())
                            .orElseGet(() -> ProductSize.builder()
                                    .product(product)
                                    .sold(0L)
                                    .build());
                    productSize.setDeleted(false);
                    productSize.setSize(productSizeRequest.getSize());
                    productSize.setStock(productSizeRequest.getStock());
                    productSize.setPrice(productSizeRequest.getPrice());
                    productSize.setDiscountPrice(productSizeRequest.getDiscountPrice());
                    productSize.setDiscountRate(productSizeRequest.getDiscountRate());
                    return productSizeRepository.save(productSize);
                }).distinct().toList();
    }

    @Override
    public List<ProductSize> getProductSizesByIds(List<Long> ids) {
        return productSizeRepository.findAllById(ids);
    }

    @Override
    public void updateStockAndSold(List<ProductSize> productSizes) {
        productSizeRepository.saveAll(productSizes);
    }

}