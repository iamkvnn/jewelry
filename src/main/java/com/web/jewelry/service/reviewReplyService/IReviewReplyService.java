package com.web.jewelry.service.reviewReplyService;

import com.web.jewelry.dto.request.ReviewReplyRequest;
import com.web.jewelry.dto.response.ReviewReplyResponse;
import com.web.jewelry.model.ReviewReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReviewReplyService {
    ReviewReply getReviewResponse(Long reviewId);
    ReviewReply createReviewResponse(ReviewReplyRequest reviewReply);
    ReviewReplyResponse convertToResponse(ReviewReply reviewReply);
}
