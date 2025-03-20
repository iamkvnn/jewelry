package com.web.jewelry.dto.request;

import com.web.jewelry.model.Review;
import lombok.Data;


@Data
public class ReviewReplyRequest {
    private String content;
    private Long reviewId;
}
