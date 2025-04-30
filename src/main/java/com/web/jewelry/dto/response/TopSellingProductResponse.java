package com.web.jewelry.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TopSellingProductResponse {
    private Long productId;
    private String productTitle;
    private String productImage;
    private Long totalQuantity = 0L;
}
