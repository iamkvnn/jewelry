package com.web.jewelry.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class MonthlyCategoryRevenueResponse {
    private int month;
    private List<CategoryRevenueItemResponse> categoryRevenueItems;
}
