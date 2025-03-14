package com.web.jewelry.service.productSize;

import com.web.jewelry.dto.request.ProductSizeRequest;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.ProductSize;
import com.web.jewelry.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductSizeService implements IProductSizeService {
    private final ProductSizeRepository productSizeRepository;

    @Override
    public ProductSize getProductSize(Long id) {
        return productSizeRepository.findById(id).orElseThrow(() -> new AlreadyExistException("Product size not found"));
    }

    @Override
    public List<ProductSize> addProductSize(Product product, List<ProductSizeRequest> request) {
        return request.stream().map(productSizeRequest -> {
            if (productSizeRepository.existsBySizeAndProductId(productSizeRequest.getSize(), product.getId())) {
                throw new AlreadyExistException("Size variant " + productSizeRequest.getSize() + " already exist for this product");
            }
            return productSizeRepository.save(ProductSize.builder()
                    .product(product)
                    .size(productSizeRequest.getSize())
                    .stock(productSizeRequest.getStock())
                    .price(productSizeRequest.getPrice())
                    .sold(0L)
                    .discountPrice(productSizeRequest.getDiscountPrice())
                    .discountRate(productSizeRequest.getDiscountRate())
                    .build());
        }).distinct().toList();
    }

    @Override
    public List<ProductSize> updateProductSize(Product product, List<ProductSizeRequest> request) {
        product.getProductSizes().stream()
                .filter(productSize -> request.stream()
                        .noneMatch(productSizeRequest -> productSizeRequest.getSize().equals(productSize.getSize())))
                .forEach(productSize -> productSizeRepository.deleteByProductIdAndSize(product.getId(), productSize.getSize()));
        return request.stream()
                .map(productSizeRequest -> {
                    ProductSize productSize = productSizeRepository.findBySizeAndProductId(productSizeRequest.getSize(), product.getId())
                            .orElseGet(() -> ProductSize.builder()
                                    .product(product)
                                    .build());
                    productSize.setSold(productSizeRequest.getSold());
                    productSize.setSize(productSizeRequest.getSize());
                    productSize.setStock(productSizeRequest.getStock());
                    productSize.setPrice(productSizeRequest.getPrice());
                    productSize.setDiscountPrice(productSizeRequest.getDiscountPrice());
                    productSize.setDiscountRate(productSizeRequest.getDiscountRate());
                    return productSizeRepository.save(productSize);
                }).distinct().toList();
    }

    @Override
    public void decreaseStock(Long id, Long quantity) {
        ProductSize productSize = getProductSize(id);
        if (productSize.getStock() < quantity) {
            throw new ResourceNotFoundException("Not enough stock for this product");
        }
        productSize.setStock(productSize.getStock() - quantity);
        productSizeRepository.save(productSize);
    }

    @Override
    public void increaseStock(Long id, Long quantity) {
        ProductSize productSize = getProductSize(id);
        productSize.setStock(productSize.getStock() + quantity);
        productSizeRepository.save(productSize);
    }

    @Override
    public void increaseSold(Long id, Long quantity) {
        ProductSize productSize = getProductSize(id);
        productSize.setSold(productSize.getSold() + quantity);
        productSizeRepository.save(productSize);
    }

    @Override
    public void decreaseSold(Long id, Long quantity) {
        ProductSize productSize = getProductSize(id);
        productSize.setSold(productSize.getSold() - quantity);
        productSizeRepository.save(productSize);
    }
}
