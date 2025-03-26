package com.web.jewelry.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private String content;
    private Long rating;
    private LocalDateTime createdAt;
    private Long productId;
    private UserResponse reviewer;
}
