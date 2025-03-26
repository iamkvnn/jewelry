package com.web.jewelry.service.review;

import com.web.jewelry.dto.request.ReviewRequest;
import com.web.jewelry.dto.response.ReviewResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Product;
import com.web.jewelry.model.Review;
import com.web.jewelry.model.User;
import com.web.jewelry.repository.ReviewRepository;
import com.web.jewelry.service.product.IProductService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final IUserService userService;
    private final IProductService productService;

    private final ModelMapper modelMapper;


    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    @Override
    public Review createReview(ReviewRequest request) {
        Customer customer = (Customer) userService.getCurrentUser();
        Product product = productService.getProductById(request.getProductId());

        Review review = Review.builder()
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .reviewer(customer)
                .product(product)
                .build();
        return reviewRepository.save(review);
    }

    @Override
    public Page<Review> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findAllByProductId(productId, pageable);
    }

    @Override
    public ReviewResponse convertToResponse(Review review) {
        return modelMapper.map(review, ReviewResponse.class);
    }

    @Override
    public Page<ReviewResponse> convertToPageResponse(Page<Review> reviews) {
        return reviews.map(this::convertToResponse);
    }
}
