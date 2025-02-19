package com.web.jewelry.dto.request;

import com.web.jewelry.dto.response.CollectionResponse;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private Long discountPrice;
    private Long discountRate;
    private Long stock;
    private CategoryRequest category;
    private CollectionRequest collection;
    private List<FeatureValueRequest> features;
    private List<SizeVariantRequest> sizeVariants;
}
