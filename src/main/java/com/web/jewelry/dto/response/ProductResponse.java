package com.web.jewelry.dto.response;

import com.web.jewelry.model.Category;
import lombok.Data;

import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private Long discountPrice;
    private Long discountRate;
    private Long stock;
    private CategoryResponse category;
    private CollectionResponse collection;
    private List<FeatureValueResponse> features;
    private List<SizeVariantResponse> sizeVariants;
    private List<ImageResponse> images;
}
