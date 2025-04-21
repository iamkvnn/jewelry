package com.web.jewelry.dto.response;

import com.web.jewelry.model.ProductSize;
import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private ProductResponse product;
    private ProductSize productSize;
    private Long quantity;
    private Long price;
    private Long discountPrice;
    private Long totalPrice;
    private ReturnItemResponse returnItem;
}
