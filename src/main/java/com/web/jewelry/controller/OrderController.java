package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import com.web.jewelry.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.web.jewelry.service.order.IOrderService;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<ApiResponse> getCustomerOrders(@PathVariable Long id, @RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "30") Long size) {
        Page<Order> orders = orderService.getCustomerOrders(id, page, size);
        Page<OrderResponse> orderResponses = orderService.convertToResponse(orders);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponses));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getOrders(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "30") Long size) {
        Page<Order> orders = orderService.getOrders(page, size);
        Page<OrderResponse> orderResponses = orderService.convertToResponse(orders);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponses));
    }

    @PostMapping("/place/{customerId}")
    public ResponseEntity<ApiResponse> placeOrder(@PathVariable Long customerId) {
        Order order = orderService.placeOrder(customerId);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }

    @PutMapping("{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long id, @RequestParam EOrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateOrderInfo(@PathVariable Long id, @RequestParam Long customerId, @RequestParam(required = false) String note,
                                                       @RequestParam(required = false) EPaymentMethod paymentMethod, @RequestParam(required = false) EShippingMethod shippingMethod) {
        Order order = orderService.updateOrderInfo(id, customerId, note, paymentMethod, shippingMethod);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }

    @DeleteMapping("{id}/cancel-checkout")
    public ResponseEntity<ApiResponse> cancelCheckout(@PathVariable Long id, @RequestParam Long customerId) {
        orderService.cancelCheckout(customerId, id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
