package com.web.jewelry.repository;

import com.web.jewelry.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    Optional<ReviewReply> findByReviewId(Long reviewId);
}
