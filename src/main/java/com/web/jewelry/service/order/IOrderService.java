package com.web.jewelry.service.order;

import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.model.Order;
import org.springframework.data.domain.Page;

public interface IOrderService {
    void cancelCheckout(Long customerId, Long orderId);
    Order placeOrder(Long customerId);
    Order updateOrderInfo(Long orderId, Long customerId, String note, EPaymentMethod paymentMethod, EShippingMethod shippingMethod);
    Page<Order> getOrders(Long page, Long size);
    Order getOrder(Long orderId);
    Page<Order> getCustomerOrders(Long customerId, Long page, Long size);
    Page<Order> getMyOrders();
    Order updateOrderStatus(Long orderId, EOrderStatus status);
    Long getEstimateShippingFee(String district, String province, EShippingMethod method);
    OrderResponse convertToResponse(Order order);
    Page<OrderResponse> convertToResponse(Page<Order> orders);
    void completeOrder(Long orderId);
}
