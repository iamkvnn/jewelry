package com.web.jewelry.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSoldByCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private Long totalSales;
}
