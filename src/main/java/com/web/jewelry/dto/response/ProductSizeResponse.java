package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class ProductSizeResponse {
    private Long id;
    private String size;
    private Long stock;
    private Long price;
    private Long sold;
    private Long discountPrice;
    private Long discountRate;
    private boolean isDeleted;
}
