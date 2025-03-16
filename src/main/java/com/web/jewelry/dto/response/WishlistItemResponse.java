package com.web.jewelry.dto.response;

import com.web.jewelry.model.Product;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistItemResponse {
    private Long id;
    private ProductResponse product;
}
