package com.web.jewelry.service.product;

import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    Page<ProductResponse> getSearchAndFilterProducts(String title, List<Long> categories, String material, Long minPrice, Long maxPrice, List<String> sizes, String dir, Pageable pageable);
    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse updateProduct(Long productId, ProductRequest request);
    ProductResponse addProduct(ProductRequest request);
    void deleteProduct(Long productId);
    Product getProductById(Long productId);
    ProductResponse convertToProductResponse(Product product);
    Page<ProductResponse> convertToProductResponses(Page<Product> products);
    boolean existsByTitle(String title);
}
