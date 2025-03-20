package com.web.jewelry.controller;

import com.web.jewelry.dto.request.ReviewReplyRequest;
import com.web.jewelry.dto.request.ReviewRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.ReviewReplyResponse;
import com.web.jewelry.dto.response.ReviewResponse;
import com.web.jewelry.model.Review;
import com.web.jewelry.model.ReviewReply;
import com.web.jewelry.service.review.IReviewService;
import com.web.jewelry.service.reviewReplyService.IReviewReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/reviews")
public class ReviewController {
    private final IReviewService reviewService;
    private final IReviewReplyService reviewReplyService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse>  getProductReviews(@PathVariable Long productId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {
        Page<Review> reviews = reviewService.getProductReviews(productId, PageRequest.of(page-1, size));
        Page<ReviewResponse> responses = reviewService.convertToPageResponse(reviews);
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @PostMapping("/add-review")
    public ResponseEntity<ApiResponse> createReview(@RequestBody ReviewRequest request) {
        Review review = reviewService.createReview(request);
        ReviewResponse response = reviewService.convertToResponse(review);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }
    @PostMapping("/add-response")
    public ResponseEntity<ApiResponse> createReviewResponse(@RequestBody ReviewReplyRequest request) {
        ReviewReply reviewReply = reviewReplyService.createReviewResponse(request);
        ReviewReplyResponse response = reviewReplyService.convertToResponse(reviewReply);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/response/{reviewId}")
    public ResponseEntity<ApiResponse> getReviewResponse(@PathVariable Long reviewId) {
        ReviewReply reviewReply = reviewReplyService.getReviewResponse(reviewId);
        if(reviewReply == null) {
            return ResponseEntity.ok(new ApiResponse("200", "Chưa có phản hồi đánh giá.", null));
        }
        else{
            ReviewReplyResponse response = reviewReplyService.convertToResponse(reviewReply);
            return ResponseEntity.ok(new ApiResponse("200", "Success", response));
        }
    }
}
