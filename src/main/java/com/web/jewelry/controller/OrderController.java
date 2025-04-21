package com.web.jewelry.controller;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.response.AddressResponse;
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


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable String id) {
        Order order = orderService.getOrder(id);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse> getCustomerOrders(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "30") Long size) {
        Page<Order> orders = orderService.getMyOrders(page, size);
        Page<OrderResponse> orderResponses = orderService.convertToResponse(orders);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponses));
    }

    @GetMapping("/shipping-fee")
    public ResponseEntity<ApiResponse> getEstimateShippingFee(@RequestBody AddressResponse address, @RequestParam EShippingMethod method) {
        Long fee = orderService.getEstimateShippingFee(address.getDistrict(), address.getProvince(), method);
        return ResponseEntity.ok(new ApiResponse("200", "Success", fee));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getOrders(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "30") Long size) {
        Page<Order> orders = orderService.getOrders(page, size);
        Page<OrderResponse> orderResponses = orderService.convertToResponse(orders);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponses));
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.placeOrder(orderRequest);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable String id, @RequestParam EOrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        OrderResponse orderResponse = orderService.convertToResponse(order);
        return ResponseEntity.ok(new ApiResponse("200", "Success", orderResponse));
    }
}
