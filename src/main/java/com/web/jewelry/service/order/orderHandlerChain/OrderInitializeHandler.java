package com.web.jewelry.service.order.orderHandlerChain;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.model.Address;
import com.web.jewelry.model.Cart;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.address.IAddressService;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.utils.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.web.jewelry.model.Order;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class OrderInitializeHandler extends OrderHandler {
    private final ICartService cartService;
    private final IAddressService addressService;
    private final OrderRepository orderRepository;

    @Override
    public Order process(OrderHandlerContext context) {
        Cart cart = cartService.getMyCart();
        context.setCart(cart);
        OrderRequest request = context.getOrderRequest();
        Address address = addressService.getAddressById(request.getShippingAddress().getId());
        context.setOrder(initializeOrder(request, cart, address));
        return processNext(context);
    }

    private Order initializeOrder(OrderRequest orderRequest, Cart cart, Address address) {
        LocalDateTime orderDate = LocalDateTime.now();
        String orderCode = OrderIdGenerator.getInstance().generateCode(orderDate);
        while (orderRepository.existsById(orderCode)) {
            orderCode = OrderIdGenerator.getInstance().generateCode(orderDate);
        }
        return Order.builder()
                .id(orderCode)
                .shippingAddress(address)
                .shippingMethod(orderRequest.getShippingMethod())
                .paymentMethod(orderRequest.getPaymentMethod())
                .status(EOrderStatus.PENDING)
                .note(orderRequest.getNote())
                .orderDate(orderDate)
                .customer(cart.getCustomer())
                .isReviewed(false)
                .totalProductPrice(orderRequest.getTotalProductPrice())
                .shippingFee(orderRequest.getShippingFee())
                .freeShipDiscount(orderRequest.getFreeShipDiscount())
                .promotionDiscount(orderRequest.getPromotionDiscount())
                .totalPrice(orderRequest.getTotalPrice())
                .build();
    }
}
