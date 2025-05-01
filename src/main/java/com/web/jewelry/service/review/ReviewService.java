package com.web.jewelry.service.review;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.ReviewRequest;
import com.web.jewelry.dto.response.ReviewResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.repository.ReviewRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.order.IOrderService;
import com.web.jewelry.service.product.IProductService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final IUserService userService;
    private final IProductService productService;
    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final INotificationService notificationService;

    private final ModelMapper modelMapper;

    @Override
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    @Transactional
    @Override
    public void createReview(String orderId, List<ReviewRequest> requests) {
        Customer customer = (Customer) userService.getCurrentUser();
        Order order = orderService.getOrder(orderId);
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Order does not belong to this customer");
        }
        if (order.isReviewed()) {
            throw new AlreadyExistException("Order has already been reviewed");
        }
        if (order.getStatus() != EOrderStatus.COMPLETED) {
            throw new BadRequestException("Order is not completed");
        }
        Set<Long> productIds = new HashSet<>();
        Set<Long> orderItemIds = order.getOrderItems().stream().map(OrderItem::getProduct).map(Product::getId).collect(Collectors.toSet());

        List<Review> reviews = new ArrayList<>();
        requests.forEach(reviewRequest -> {
            if (productIds.contains(reviewRequest.getProductId())) {
                throw new BadRequestException("Duplicate product in review");
            }
            productIds.add(reviewRequest.getProductId());
            Product product = productService.getProductById(reviewRequest.getProductId());
            reviews.add(Review.builder()
                    .content(reviewRequest.getContent())
                    .rating(reviewRequest.getRating())
                    .product(product)
                    .reviewer(customer)
                    .createdAt(LocalDateTime.now())
                    .build());
        });
        if (!orderItemIds.containsAll(productIds)) {
            throw new BadRequestException("Order does not contain all products");
        }
        reviewRepository.saveAll(reviews);
        order.setReviewed(true);
        orderRepository.save(order);
        notificationService.sendNotificationToAllStaff(NotificationRequest.builder()
                .title("Có đánh giá mới")
                .content("Khách hàng " + customer.getFullName() + " đã đánh giá các sản phẩm trong đơn hàng " + order.getId() + " của họ")
                .build());
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
