package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class SizeVariantResponse {
    private Long id;
    private String size;
    private Long stock;
    private Long price;
    private Long discountPrice;
    private Long discountRate;
}
