package com.web.jewelry.service.order;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.model.Order;
import org.springframework.data.domain.Page;

public interface IOrderService {
    Order placeOrder(Long customerId, OrderRequest orderRequest);
    Page<Order> getOrders(Long page, Long size);
    Order getOrder(Long orderId);
    Page<Order> getCustomerOrders(Long customerId, Long page, Long size);
    Page<Order> getMyOrders();
    Order updateOrderStatus(Long orderId, EOrderStatus status);
    Long getEstimateShippingFee(String district, String province, EShippingMethod method);
    OrderResponse convertToResponse(Order order);
    Page<OrderResponse> convertToResponse(Page<Order> orders);
}
