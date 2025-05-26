package com.web.jewelry.service.product;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheProductService implements IProductService {
    private final IProductService productService;
    private final Cache<Long, Product> productCache;
    private final Cache<String, Page<ProductResponse>> productCategoryCache;
    private final Cache<String, Page<ProductResponse>> producFiltertCache;

    public CacheProductService(ProductService productService) {
        this.productService = productService;
        this.productCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        this.productCategoryCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        this.producFiltertCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        try {
            String filter = String.join(",", String.valueOf(categoryId), String.valueOf(page), String.valueOf(size));
            return productCategoryCache.get(filter, () -> {
                Page<ProductResponse> product = productService.getProductsByCategory(categoryId, page, size);
                productCategoryCache.put(filter, product);
                return product;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to get product from cache" + e.getMessage());
        }
    }

    public Product getProductById(Long id) {
        try {
            return productCache.get(id, () -> {
                Product product = productService.getProductById(id);
                productCache.put(id, product);
                return product;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to get product from cache" + e.getMessage());
        }
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        clearCacheById(productId);
        return productService.updateProduct(productId, request);
    }

    @Override
    public ProductResponse addProduct(ProductRequest request) {
        return productService.addProduct(request);
    }

    @Override
    public void deleteProduct(Long productId) {
        clearCacheById(productId);
        productService.deleteProduct(productId);
    }

    @Override
    public ProductResponse convertToProductResponse(Product product) {
        return productService.convertToProductResponse(product);
    }

    @Override
    public Page<ProductResponse> convertToProductResponses(Page<Product> products) {
        return productService.convertToProductResponses(products);
    }

    @Override
    public Page<ProductResponse> getSearchAndFilterProducts(String title, List<Long> categories, String material, Long minPrice, Long maxPrice, List<String> sizes, int page, int size) {
        try {
            String filter = String.join(",", title, String.valueOf(categories), String.valueOf(minPrice), String.valueOf(maxPrice), String.valueOf(sizes), material, String.valueOf(page), String.valueOf(size));
            return producFiltertCache.get(filter, () -> {
                Page<ProductResponse> product = productService.getSearchAndFilterProducts(title, categories, material, minPrice, maxPrice, sizes, page, size);
                producFiltertCache.put(filter, product);
                return product;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to get product from cache" + e.getMessage());
        }
    }

    private void clearCacheById(Long id) {
        productCache.invalidate(id);
        productCategoryCache.asMap()
                .forEach((key, value) -> {
                    if (value.getContent().stream().anyMatch(product -> product.getId().equals(id))) {
                        productCategoryCache.invalidate(key);
                    }
                });
        producFiltertCache.asMap()
                .forEach((key, value) -> {
                    if (value.getContent().stream().anyMatch(product -> product.getId().equals(id))) {
                        producFiltertCache.invalidate(key);
                    }
                });
    }
}
