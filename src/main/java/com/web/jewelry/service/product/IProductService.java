package com.web.jewelry.service.product;

import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import org.springframework.data.domain.Page;

public interface IProductService {
    ProductResponse updateProduct(Long productId, ProductRequest request);
    ProductResponse addProduct(ProductRequest request);
    void deleteProduct(Long productId);
    Product getProductById(Long productId);
    ProductResponse convertToProductResponse(Product product);
    Page<ProductResponse> convertToProductResponses(Page<Product> products);
    boolean existsByTitle(String title);
}
