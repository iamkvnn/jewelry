package com.web.jewelry.service.order;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.ReturnOrderRequest;
import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface IOrderService {
    Order placeOrder(OrderRequest orderRequest);
    Page<Order> getOrders(EOrderStatus status, Long page, Long size, String query);
    Order getOrder(String orderId);
    Page<Order> getMyOrders(EOrderStatus status, Long page, Long size);
    Order updateOrderStatus(String orderId, EOrderStatus status);
    Long getEstimateShippingFee(String district, String province, EShippingMethod method);

    @Transactional
    void returnOrderItem(ReturnOrderRequest request);

    OrderResponse convertToResponse(Order order);
    Page<OrderResponse> convertToResponse(Page<Order> orders);
}
