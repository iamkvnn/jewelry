package com.web.jewelry.service.order;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOderService {
    Order placeOrder(Long customerId, OrderRequest request);
    Page<Order> getOrders(Long page, Long size);
    Order getOrder(Long orderId);
    Page<Order> getCustomerOrders(Long customerId, Long page, Long size);
    Page<Order> getMyOrders();
    Order updateOrderStatus(Long orderId, EOrderStatus status);
    OrderResponse convertToResponse(Order order);
    Page<OrderResponse> convertToResponse(Page<Order> orders);
}
