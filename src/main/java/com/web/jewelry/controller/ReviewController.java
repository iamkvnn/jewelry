package com.web.jewelry.controller;

import com.web.jewelry.dto.request.ReviewReplyRequest;
import com.web.jewelry.dto.request.ReviewRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.ReviewResponse;
import com.web.jewelry.model.Review;
import com.web.jewelry.service.review.IReviewService;
import com.web.jewelry.service.reviewReplyService.IReviewReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/reviews")
public class ReviewController {
    private final IReviewService reviewService;
    private final IReviewReplyService reviewReplyService;

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllReviews(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<Review> reviews = reviewService.getAllReviews(PageRequest.of(page - 1, size,
                                    Sort.by(Sort.Order.asc("isResponded"), Sort.Order.asc("rating"), Sort.Order.desc("createdAt"))));
        Page<ReviewResponse> responses = reviewService.convertToPageResponse(reviews);
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse>  getProductReviews(@PathVariable Long productId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {
        Page<Review> reviews = reviewService.getProductReviews(productId, PageRequest.of(page - 1, size,
                Sort.by(Sort.Order.asc("isResponded"), Sort.Order.desc("rating"))));
        Page<ReviewResponse> responses = reviewService.convertToPageResponse(reviews);
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/add-review")
    public ResponseEntity<ApiResponse> createReview(@RequestParam String orderId, @RequestBody List<ReviewRequest> requests) {
        reviewService.createReview(orderId, requests);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/add-response")
    public ResponseEntity<ApiResponse> createReviewResponse(@RequestBody ReviewReplyRequest request) {
        reviewReplyService.createReviewResponse(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
