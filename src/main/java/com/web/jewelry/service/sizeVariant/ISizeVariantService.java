package com.web.jewelry.service.sizeVariant;

import com.web.jewelry.dto.request.SizeVariantRequest;
import com.web.jewelry.dto.response.SizeVariantResponse;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.SizeVariant;

import java.util.List;

public interface ISizeVariantService {
    List<SizeVariant> addSizeVariant(Product product, List<SizeVariantRequest> request);
    List<SizeVariant> updateSizeVariant(Product product, List<SizeVariantRequest> request);
}
