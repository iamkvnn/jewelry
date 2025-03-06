package com.web.jewelry.service.productSize;

import com.web.jewelry.dto.request.ProductSizeRequest;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.ProductSize;

import java.util.List;

public interface IProductSizeService {
    List<ProductSize> addProductSize(Product product, List<ProductSizeRequest> request);
    List<ProductSize> updateProductSize(Product product, List<ProductSizeRequest> request);
}
