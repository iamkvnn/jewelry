package com.web.jewelry.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRevenueItemResponse {
    private Long categoryId;
    private String categoryName;
    private Long revenue;
}
