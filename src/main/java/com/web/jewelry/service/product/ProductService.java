package com.web.jewelry.service.product;

import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.enums.EProductStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.ProductRepository;
import com.web.jewelry.service.category.ICategoryService;
import com.web.jewelry.service.collection.ICollectionService;
import com.web.jewelry.service.feature.IFeatureService;
import com.web.jewelry.service.feature.IFeatureValueService;
import com.web.jewelry.service.sizeVariant.ISizeVariantService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final ICategoryService categoryService;
    private final ICollectionService collectionService;
    private final ISizeVariantService sizeVariantService;
    private final IFeatureValueService featureValueService;
    private final ModelMapper modelMapper;

    @Override
    public List<ProductResponse> getAllProducts() {
        return convertToProductResponses(productRepository.findAll());
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        return productRepository.findById(productId)
                .map(product -> {
                    if (existsByTitle(request.getTitle()) && !product.getTitle().equals(request.getTitle())) {
                        throw new AlreadyExistException("Product title already exists please check again!");
                    }
                    Collection collection = request.getCollection() != null ? collectionService.getCollectionById(request.getCollection().getId()) : null;
                    Category category = request.getCategory() != null ? categoryService.getCategoryById(request.getCategory().getId()) : null;
                    return updateExistingProduct(product, request, collection, category);
                })
                .map(productRepository::save)
                .map(product -> {
                    product.setFeatures(request.getFeatures() != null ? featureValueService.updateProductFeature(product, request.getFeatures()) : null);
                    product.setSizeVariants(request.getSizeVariants() != null ? sizeVariantService.updateSizeVariant(product, request.getSizeVariants()) : null);
                    return product;
                })
                .map(this::convertToProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Product updateExistingProduct(Product product, ProductRequest request, Collection collection, Category category) {
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setDiscountRate(request.getDiscountRate());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setCollection(collection);
        return product;
    }

    @Transactional
    @Override
    public ProductResponse addProduct(ProductRequest request) {
        if (existsByTitle(request.getTitle())) {
            throw new AlreadyExistException("Product title already exists please check again!");
        }
        Collection collection = request.getCollection() != null ? collectionService.getCollectionById(request.getCollection().getId()) : null;
        Category category = request.getCategory() != null ? categoryService.getCategoryById(request.getCategory().getId()) : null;
        Product product = productRepository.save(Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .discountRate(request.getDiscountRate())
                .stock(request.getStock())
                .sold(0L)
                .category(category)
                .collection(collection)
                .status(EProductStatus.IN_STOCK)
                .createdAt(LocalDateTime.now())
                .build());
        product.setFeatures(request.getFeatures() != null ? featureValueService.addProductFeatures(product, request.getFeatures()) : null);
        product.setSizeVariants(request.getSizeVariants() != null ? sizeVariantService.addSizeVariant(product, request.getSizeVariants()) : null);
        return convertToProductResponse(product);
    }

    @Override
    public void deleteProduct(Long productId) {
        productRepository.findById(productId).ifPresentOrElse(productRepository::delete,
                                () -> {throw new ResourceNotFoundException("Product not found");
                                });
    }

    @Override
    public Page<ProductResponse> findByTitleContaining(String title, Pageable pageable) {
        return productRepository.findByTitleContaining(title, pageable);
    }

    @Override
    public ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        response.setFeatures(product.getFeatures() != null ? featureValueService.convertToFeatureValueResponses(product.getFeatures()) : null);
        return response;
    }

    @Override
    public List<ProductResponse> convertToProductResponses(List<Product> products) {
        return products.stream().map(this::convertToProductResponse).toList();
    }

    @Override
    public boolean existsByTitle(String title) {
        return productRepository.existsByTitle(title);
    }
}
