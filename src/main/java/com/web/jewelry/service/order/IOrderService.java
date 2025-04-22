package com.web.jewelry.service.order;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.ReturnItemRequest;
import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IOrderService {
    Order placeOrder(OrderRequest orderRequest);
    Page<Order> getOrders(Long page, Long size);
    Order getOrder(String orderId);
    Page<Order> getMyOrders(Long page, Long size);
    Order updateOrderStatus(String orderId, EOrderStatus status);
    Long getEstimateShippingFee(String district, String province, EShippingMethod method);
    void returnOrderItem(ReturnItemRequest request);
    OrderResponse convertToResponse(Order order);
    Page<OrderResponse> convertToResponse(Page<Order> orders);
}
