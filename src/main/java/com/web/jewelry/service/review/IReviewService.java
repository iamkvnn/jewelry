package com.web.jewelry.service.review;

import com.web.jewelry.dto.request.ReviewRequest;
import com.web.jewelry.dto.response.ReviewResponse;
import com.web.jewelry.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IReviewService {
    Page<Review> getAllReviews(Pageable pageable);
    Review getReviewById(Long id);
    void createReview(String orderId, List<ReviewRequest> requests);
    Page<Review> getProductReviews(Long productId, Pageable pageable);
    ReviewResponse convertToResponse(Review review);
    Page<ReviewResponse> convertToPageResponse(Page<Review> reviews);
}
