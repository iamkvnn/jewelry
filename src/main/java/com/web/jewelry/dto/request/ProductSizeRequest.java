package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class ProductSizeRequest {
    private Long id;
    private String size;
    private Long stock;
    private Long price;
    private Long discountPrice;
    private Long discountRate;
}
