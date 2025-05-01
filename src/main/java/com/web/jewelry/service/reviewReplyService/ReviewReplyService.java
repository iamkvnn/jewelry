package com.web.jewelry.service.reviewReplyService;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.ReviewReplyRequest;
import com.web.jewelry.dto.response.ReviewReplyResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Review;
import com.web.jewelry.model.ReviewReply;
import com.web.jewelry.model.Staff;
import com.web.jewelry.repository.ReviewReplyRepository;
import com.web.jewelry.repository.ReviewRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.order.IOrderService;
import com.web.jewelry.service.review.IReviewService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewReplyService implements IReviewReplyService {
    private final ReviewReplyRepository reviewReplyRepository;
    private final IReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final IUserService userService;

    @Transactional
    @Override
    public void createReviewResponse(ReviewReplyRequest request) {
        Review review = reviewService.getReviewById(request.getReviewId());
        if (review.isResponded()) {
            throw new ResourceNotFoundException("Review already responded");
        }
        Staff staff = (Staff) userService.getCurrentUser();
        ReviewReply reviewResponse = ReviewReply.builder()
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .review(review)
                .responseBy(staff)
                .build();
        review.setResponded(true);
        reviewRepository.save(review);
        reviewReplyRepository.save(reviewResponse);
    }
}
