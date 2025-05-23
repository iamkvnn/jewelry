package com.web.jewelry.service.product;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.request.ProductSizeRequest;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.enums.EProductStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.ProductRepository;
import com.web.jewelry.service.category.ICategoryService;
import com.web.jewelry.service.collection.ICollectionService;
import com.web.jewelry.service.attribute.IAttributeValueService;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.observer.ProductSizeObservable;
import com.web.jewelry.service.productSize.IProductSizeService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final ICategoryService categoryService;
    private final ICollectionService collectionService;
    private final IProductSizeService productSizeService;
    private final IAttributeValueService attributeValueService;
    private final IUserService userService;
    private final ModelMapper modelMapper;
    private final INotificationService notificationService;
    private final ProductSizeObservable productSizeObservable;

    @Override
    public Page<ProductResponse> getSearchAndFilterProducts(String title, List<Long> categories, String material, Long minPrice,
                                                     Long maxPrice, List<String> sizes, int page, int size) {
        Page<Product> products = productRepository.findByFilters(title, categories, material, minPrice, maxPrice, sizes, PageRequest.of(page - 1, size));
        return  convertToProductResponses(products);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        return convertToProductResponses(productRepository.findAllByCategoryId(categoryId, PageRequest.of(page - 1, size)));
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        validateProductSizes(request.getProductSizes());
        AtomicBoolean isBackInStock = new AtomicBoolean(false);
        return productRepository.findById(productId)
                .map(product -> {
                    if (existsByTitle(request.getTitle()) && !product.getTitle().equals(request.getTitle())) {
                        throw new AlreadyExistException("Product title already exists please check again!");
                    }
                    Long totalStock = request.getProductSizes().stream()
                            .map(ProductSizeRequest::getStock)
                            .reduce(0L, Long::sum);
                    if (product.getStatus() == EProductStatus.OUT_OF_STOCK && totalStock > 0) {
                        product.setStatus(EProductStatus.IN_STOCK);
                        isBackInStock.set(true);
                    }
                    Collection collection = request.getCollection() != null ? collectionService.getCollectionById(request.getCollection().getId()) : null;
                    Category category = request.getCategory() != null ? categoryService.getCategoryById(request.getCategory().getId()) : null;
                    return updateExistingProduct(product, request, collection, category);
                })
                .map(productRepository::save)
                .map(product -> {
                    Optional.ofNullable(request.getAttributes())
                            .ifPresent(attributes -> product.setAttributes(attributeValueService.updateProductAttributes(product, attributes)));
                    product.setProductSizes(productSizeService.updateProductSize(product, request.getProductSizes()));
                    long totalStock = product.getProductSizes().stream()
                            .map(ProductSize::getStock)
                            .reduce(0L, Long::sum);
                    Long bigDiscount = product.getProductSizes().stream()
                            .filter(productSize -> productSize.getDiscountRate() != null && productSize.getDiscountRate() >= 20)
                            .map(ProductSize::getDiscountRate)
                            .max(Comparator.naturalOrder())
                            .orElse(0L);
                    if (isBackInStock.get())
                        notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                                        .title("Sản phẩm '" + product.getTitle() + "' đã có hàng.")
                                        .content("Sản phẩm '" + product.getTitle() + "' đã có hàng. Hãy nhanh tay đặt hàng trước khi hết hàng.")
                                        .customerIds(product.getWishlistItems().stream()
                                                .map(WishlistItem::getCustomer)
                                                .map(Customer::getId)
                                                .collect(Collectors.toSet()))
                                        .isEmail(true)
                                .build());
                    if (totalStock <= 3 && !isBackInStock.get() && product.getStatus() == EProductStatus.IN_STOCK)
                        notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                                        .title("Sản phẩm '" + product.getTitle() + "' mà bạn quan tâm sắp hết hàng.")
                                        .content("Sản phẩm '" + product.getTitle() + "' mà bạn quan tâm sắp hết hàng. Hãy nhanh tay đặt hàng trước khi hết hàng.")
                                        .customerIds(product.getWishlistItems().stream()
                                                .map(WishlistItem::getCustomer)
                                                .map(Customer::getId)
                                                .collect(Collectors.toSet()))
                                .build());
                    if (bigDiscount >= 20 && totalStock > 0)
                        notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                                        .title("Sản phẩm '" + product.getTitle() + "' đang có khuyến mãi lớn.")
                                        .content("Sản phẩm '" + product.getTitle() + "' đang có chương trình khuyến mãi lớn lên tới " + bigDiscount  +
                                                "%.\nHãy nhanh tay đặt hàng trước khi hết hàng.")
                                        .customerIds(product.getWishlistItems().stream()
                                                .map(WishlistItem::getCustomer)
                                                .map(Customer::getId)
                                                .collect(Collectors.toSet()))
                                        .isEmail(true)
                                .build());
                    return product;
                })
                .map(this::convertToProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Product updateExistingProduct(Product product, ProductRequest request, Collection collection, Category category) {
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setMaterial(request.getMaterial());
        product.setUpdatedAt(LocalDateTime.now());
        product.setCategory(category);
        product.setCollection(collection);
        return product;
    }

    @Transactional
    @Override
    public ProductResponse addProduct(ProductRequest request) {
        validateProductSizes(request.getProductSizes());
        if (existsByTitle(request.getTitle())) {
            throw new AlreadyExistException("Product title already exists please check again!");
        }
        Collection collection = request.getCollection() != null ? collectionService.getCollectionById(request.getCollection().getId()) : null;
        Category category = request.getCategory() != null ? categoryService.getCategoryById(request.getCategory().getId()) : null;
        Product product = productRepository.save(Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .material(request.getMaterial())
                .category(category)
                .collection(collection)
                .status(EProductStatus.IN_STOCK)
                .createdAt(LocalDateTime.now())
                .build());
        Optional.ofNullable(request.getAttributes())
                .ifPresent(attributes -> product.setAttributes(attributeValueService.addProductAttributes(product, attributes)));
        product.setProductSizes(productSizeService.addProductSize(product, request.getProductSizes()));
        notificationService.sendNotificationToSpecificCustomer(NotificationRequest.builder()
                .title("Có sản phẩm mới! Nhanh tay đặt hàng nào.")
                .content("Sản phẩm mới '" + product.getTitle() + "' vừa được thêm vào cừa hàng. Hãy ghé thăm cửa hàng để xem thêm các thông tin về sản phẩm.")
                .customerIds(userService.getAllUerRegisteredForNews())
                .isEmail(true)
                .build());
        return convertToProductResponse(product);
    }

    private void validateProductSizes(List<ProductSizeRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new BadRequestException("Product size is required");
        }
        Map<String, Long> sizeFrequency = request.stream()
                .collect(Collectors.groupingBy(ProductSizeRequest::getSize, Collectors.counting()));

        List<String> duplicateSizes = sizeFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (!duplicateSizes.isEmpty()) {
            throw new BadRequestException("Duplicate sizes found");
        }
    }

    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        product.setStatus(EProductStatus.NOT_AVAILABLE);
        product.getProductSizes().forEach(productSize -> {
            productSize.setDeleted(true);
            productSizeObservable.notifyObservers(productSize.getId());
        });
        productRepository.save(product);
    }

    @Override
    public ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        response.setAttributes(product.getAttributes() != null ? attributeValueService.convertToAttributeValueResponses(product.getAttributes()) : null);
        return response;
    }

    @Override
    public Page<ProductResponse> convertToProductResponses(Page<Product> products) {
        return products.map(this::convertToProductResponse);
    }

    private boolean existsByTitle(String title) {
        return productRepository.existsByTitleAndAvailable(title);
    }
}