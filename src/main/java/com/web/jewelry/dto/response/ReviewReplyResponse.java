package com.web.jewelry.dto.response;

import com.web.jewelry.model.Review;
import com.web.jewelry.model.Staff;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewReplyResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse responseBy;
}
