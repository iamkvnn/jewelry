package com.web.jewelry.service.productSize;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.ProductSizeRequest;
import com.web.jewelry.enums.EProductStatus;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.ProductSize;
import com.web.jewelry.model.WishlistItem;
import com.web.jewelry.repository.ProductRepository;
import com.web.jewelry.repository.ProductSizeRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.observer.ProductSizeObservable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductSizeService implements IProductSizeService {
    private final ProductSizeRepository productSizeRepository;
    private final ProductRepository productRepository;
    private final INotificationService notificationService;
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
        Map<Product, Long> productStockMap = productSizes.stream()
                .collect(Collectors.groupingBy(ProductSize::getProduct, Collectors.summingLong(ProductSize::getStock)));
        productStockMap.forEach((product, stock) -> {
            if (stock == 0) {
                product.setStatus(EProductStatus.OUT_OF_STOCK);
                productRepository.save(product);
            } else if (stock <= 3) {
                notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                        .title("Sản phẩm '" + product.getTitle() + "' mà bạn quan tâm sắp hết hàng.")
                        .content("Sản phẩm '" + product.getTitle() + "' mà bạn quan tâm sắp hết hàng. Hãy nhanh tay đặt hàng trước khi hết hàng.")
                        .customerIds(product.getWishlistItems().stream()
                                .map(WishlistItem::getCustomer)
                                .map(Customer::getId)
                                .collect(Collectors.toSet()))
                        .build());
            }
        });
        productSizeRepository.saveAll(productSizes);
    }

}