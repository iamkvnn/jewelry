package com.web.jewelry.dto.request;

import com.web.jewelry.model.Product;
import lombok.Data;

@Data
public class ReviewRequest {
    private String content;
    private Long rating;
    private Long productId;
}
