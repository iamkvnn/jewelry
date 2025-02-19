package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class SizeVariantRequest {
    private Long id;
    private String size;
    private Long stock;
    private Long price;
    private Long discountPrice;
    private Long discountRate;
}
