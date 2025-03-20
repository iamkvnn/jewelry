package com.web.jewelry.service.reviewReplyService;

import com.web.jewelry.dto.request.ReviewReplyRequest;
import com.web.jewelry.dto.response.ReviewReplyResponse;
import com.web.jewelry.model.Review;
import com.web.jewelry.model.ReviewReply;
import com.web.jewelry.model.Staff;
import com.web.jewelry.repository.ReviewReplyRepository;
import com.web.jewelry.service.review.IReviewService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewReplyService implements IReviewReplyService {
    private final ReviewReplyRepository reviewReplyRepository;
    private final IReviewService reviewService;
    private final IUserService userService;
    private final ModelMapper modelMapper;


    @Override
    public ReviewReply getReviewResponse(Long reviewId) {
        return reviewReplyRepository.findByReviewId(reviewId).orElse(null);
    }

    @Override
    public ReviewReply createReviewResponse(ReviewReplyRequest request) {
        Review review = reviewService.getReviewById(request.getReviewId());
        Staff staff = (Staff) userService.getCurrentUser();

        ReviewReply reviewResponse = ReviewReply.builder()
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .review(review)
                .responseBy(staff)
                .build();
        return reviewReplyRepository.save(reviewResponse);
    }

    @Override
    public ReviewReplyResponse convertToResponse(ReviewReply reviewReply) {
        return modelMapper.map(reviewReply, ReviewReplyResponse.class);
    }

}
