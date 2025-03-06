package com.web.jewelry.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {
    private Long id;
    private String title;
    private String description;
    private String material;
    private CategoryRequest category;
    private CollectionRequest collection;
    private List<AttributeValueRequest> attributes;
    private List<ProductSizeRequest> productSizes;
}
