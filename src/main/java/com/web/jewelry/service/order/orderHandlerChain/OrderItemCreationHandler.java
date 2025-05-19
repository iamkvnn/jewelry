package com.web.jewelry.service.order.orderHandlerChain;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.*;
import com.web.jewelry.service.productSize.IProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderItemCreationHandler extends OrderHandler {
    private final IProductSizeService productSizeService;

    @Override
    public Order process(OrderHandlerContext context) {
        Order order = context.getOrder();
        Cart cart = context.getCart();
        OrderRequest request = context.getOrderRequest();
        List<OrderItem> orderItems = createOrderItems(order, cart, request);
        context.setOrderItems(orderItems);
        return processNext(context);
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart, OrderRequest request) {
        List<Long> productSizeIds = request.getCartItems().stream()
                .map(cartItem -> cartItem.getProductSize().getId())
                .toList();
        Map<Long, CartItem> cartItemMap = cart.getCartItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProductSize().getId(),
                        item -> item
                ));
        if (!cartItemMap.keySet().containsAll(productSizeIds)) {
            throw new ResourceNotFoundException("Product not found in your cart");
        }
        List<ProductSize> productSizes = productSizeService.getProductSizesByIds(productSizeIds);
        Map<Long, ProductSize> productSizeMap = productSizes.stream()
                .collect(Collectors.toMap(ProductSize::getId, size -> size));

        return request.getCartItems().stream()
                .map(item -> {
                    Long sizeId = item.getProductSize().getId();
                    CartItem cartItem = cartItemMap.get(sizeId);
                    ProductSize size = productSizeMap.get(sizeId);
                    if (size.getStock() < cartItem.getQuantity()) {
                        throw new ResourceNotFoundException("Not enough stock for product: " + size.getProduct().getTitle() + " - Size: " + size.getSize());
                    }
                    size.setStock(size.getStock() - cartItem.getQuantity());
                    size.setSold(size.getSold() + cartItem.getQuantity());
                    productSizeMap.put(sizeId, size);
                    return OrderItem.builder()
                            .order(order)
                            .productSize(size)
                            .product(size.getProduct())
                            .price(size.getPrice())
                            .discountPrice(size.getDiscountPrice())
                            .totalPrice(size.getDiscountPrice() * cartItem.getQuantity())
                            .quantity(cartItem.getQuantity())
                            .build();
                })
                .toList();
    }
}
