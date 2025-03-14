package com.web.jewelry.dto.response;

import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class OrderResponse {
    private Long id;
    private Set<OrderItemResponse> orderItems;
    private Long totalProductPrice;
    private AddressResponse shippingAddress;
    private EShippingMethod shippingMethod;
    private Long shippingFee;
    private Long totalPrice;
    private EPaymentMethod paymentMethod;
    private EOrderStatus status;
    private String note;
    private LocalDateTime orderDate;
    private boolean isReviewed;
}
