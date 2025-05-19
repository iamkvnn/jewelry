package com.web.jewelry.service.product;

import com.web.jewelry.dto.response.ProductResponse;
import com.web.jewelry.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IGetProductService {
    Page<ProductResponse> getSearchAndFilterProducts(String title, List<Long> categories, String material, Long minPrice, Long maxPrice, List<String> sizes, int page, int size);
    Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size);
    Product getProductById(Long productId);
}
