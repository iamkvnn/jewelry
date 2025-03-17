package com.web.jewelry.dto.response;

import com.web.jewelry.model.ProductSize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private Long id;
    private ProductResponse product;
    private ProductSizeResponse productSize;
    private Long quantity;
    private LocalDateTime addedAt;
}
