package com.web.jewelry.service.productSize;

import com.web.jewelry.dto.request.ProductSizeRequest;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.ProductSize;

import java.util.List;

public interface IProductSizeService {
    ProductSize getProductSize(Long id);
    List<ProductSize> getProductSizesByIds(List<Long> ids);
    List<ProductSize> addProductSize(Product product, List<ProductSizeRequest> request);
    List<ProductSize> updateProductSize(Product product, List<ProductSizeRequest> request);
    void updateStockAndSold(List<ProductSize> productSizes);
}
