package com.web.jewelry.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private String material;
    private CategoryResponse category;
    private CollectionResponse collection;
    private List<AttributeValueResponse> attributes;
    private List<ProductSizeResponse> productSizes;
    private List<ImageResponse> images;
}
