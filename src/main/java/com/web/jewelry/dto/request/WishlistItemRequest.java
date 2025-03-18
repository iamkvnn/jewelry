package com.web.jewelry.dto.request;

import com.web.jewelry.model.Product;
import lombok.Data;

@Data
public class WishlistItemRequest {
    private ProductRequest product;
}
