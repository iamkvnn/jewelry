package com.web.jewelry.service.order;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.response.CartItemResponse;
import com.web.jewelry.dto.response.OrderResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Cart;
import com.web.jewelry.model.CartItem;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.OrderItem;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.productSize.IProductSizeService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService implements IOderService {
    private final OrderRepository orderRepository;
    private final IProductSizeService productSizeService;
    private final ICartService cartService;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Order placeOrder(Long customerId, OrderRequest request) {
        Cart cart = cartService.getCartByCustomerId(customerId);
        Long totalPrice = request.getCartItems().stream()
                .mapToLong(CartItemResponse::getTotalPrice)
                .sum();
        Order order = Order.builder()
                .customer(cart.getCustomer())
                .totalPrice(totalPrice)
                .status(EOrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .orderDate(LocalDateTime.now())
                .build();
        List<OrderItem> orderItems = createOrderItems(order, cart, request);
        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart, OrderRequest request) {
        return request.getCartItems().stream()
                .map(CItem -> cartService.getCartItem(cart.getId(), CItem.getSize().getId()))
                .map(cartItem -> {
                    productSizeService.decreaseStock(cartItem.getProductSize().getId(), cartItem.getQuantity());
                    return OrderItem.builder()
                            .order(order)
                            .productSize(cartItem.getProductSize())
                            .quantity(cartItem.getQuantity())
                            .build();
                })
                .toList();
    }

    @Override
    public Page<Order> getOrders(Long page, Long size) {
        return orderRepository.findAll(PageRequest.of(page.intValue() - 1, size.intValue()));
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public Page<Order> getCustomerOrders(Long customerId, Long page, Long size) {
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page.intValue() - 1, size.intValue()));
    }

    @Override
    public Page<Order> getMyOrders() {
        return null;
    }

    @Override
    public Order updateOrderStatus(Long orderId, EOrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public OrderResponse convertToResponse(Order order) {
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public Page<OrderResponse> convertToResponse(Page<Order> orders) {
        return orders.map(this::convertToResponse);
    }

}
