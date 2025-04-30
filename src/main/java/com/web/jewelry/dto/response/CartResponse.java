package com.web.jewelry.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class CartResponse {
    private Long id;
    private Set<CartItemResponse> cartItems;
}
