package com.web.jewelry.dto.response;

import com.web.jewelry.enums.EProductStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private String material;
    private CategoryResponse category;
    private CollectionResponse collection;
    private EProductStatus status;
    private List<AttributeValueResponse> attributes;
    private List<ProductSizeResponse> productSizes;
    private List<ImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
