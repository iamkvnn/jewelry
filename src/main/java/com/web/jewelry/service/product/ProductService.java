package com.web.jewelry.service.product;

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
import com.web.jewelry.service.productSize.IProductSizeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final ICategoryService categoryService;
    private final ICollectionService collectionService;
    private final IProductSizeService productSizeService;
    private final IAttributeValueService attributeValueService;
    private final ModelMapper modelMapper;


    @Override
    public Page<ProductResponse> getFilteredProducts(List<Long> categories, String material, Long minPrice,
                                                     Long maxPrice, List<String> sizes, String dir, Pageable pageable) {
        Page<Product> products = productRepository.findByFilters(categories, material, minPrice, maxPrice, sizes, pageable);
        if( dir != null && (dir.equals("asc") || dir.equals("desc"))) {
            Comparator<Product> comparator = Comparator.comparing(
                    p -> p.getProductSizes().stream()
                            .map(ProductSize::getDiscountPrice)
                            .min(Long::compareTo)
                            .orElse(Long.MAX_VALUE)
            );
            if (dir.equals("desc")) {
               comparator = comparator.reversed();
            }
            List<Product> sortedProducts = products.stream().sorted(comparator).collect(Collectors.toList());
            Page<Product> response = new PageImpl<>(sortedProducts, pageable, products.getTotalElements());
            return  convertToProductResponses(response);
        }
        return  convertToProductResponses(products);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return convertToProductResponses(productRepository.findAllByCategoryId(categoryId, pageable));
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return convertToProductResponses(productRepository.findAll(pageable));
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        validateProductSizes(request.getProductSizes());
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
                    Optional.ofNullable(request.getAttributes())
                            .ifPresent(attributes -> product.setAttributes(attributeValueService.updateProductAttributes(product, attributes)));
                    product.setProductSizes(productSizeService.updateProductSize(product, request.getProductSizes()));
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

    @Override
    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        product.setStatus(EProductStatus.NOT_AVAILABLE);
        productRepository.save(product);
    }

    @Override
    public Page<ProductResponse> findByTitleContaining(String title, Pageable pageable) {
        return convertToProductResponses(productRepository.findByTitleContaining(title, pageable));
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

    @Override
    public boolean existsByTitle(String title) {
        return productRepository.existsByTitle(title);
    }
}