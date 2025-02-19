package com.web.jewelry.service.product;

import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.request.SizeVariantRequest;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(Long productId, ProductRequest request);
    ProductResponse addProduct(ProductRequest request);
    void deleteProduct(Long productId);
    Page<ProductResponse> findByTitleContaining(String title, Pageable pageable);
    Product getProductById(Long productId);
    ProductResponse convertToProductResponse(Product product);
    List<ProductResponse> convertToProductResponses(List<Product> products);
    boolean existsByTitle(String title);
}
