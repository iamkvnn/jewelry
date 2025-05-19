package com.web.jewelry.service.product;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheProductService implements IGetProductService {
    private final IGetProductService productService;
    private final Cache<Long, Product> productCache;
    private final Cache<String, Page<ProductResponse>> productCategoryCache;
    private final Cache<String, Page<ProductResponse>> producFiltertCache;

    public CacheProductService(IGetProductService productService) {
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
}
