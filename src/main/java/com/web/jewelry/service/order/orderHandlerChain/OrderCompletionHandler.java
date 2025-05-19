package com.web.jewelry.service.order.orderHandlerChain;

import com.web.jewelry.model.Order;
import com.web.jewelry.model.OrderItem;
import com.web.jewelry.model.ProductSize;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.productSize.IProductSizeService;
import com.web.jewelry.service.voucher.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderCompletionHandler extends OrderHandler {
    private final OrderRepository orderRepository;
    private final ICartService cartService;
    private final IProductSizeService productSizeService;
    private final IVoucherService voucherService;

    @Override
    public Order process(OrderHandlerContext context) {
        Order order = context.getOrder();
        order.setOrderItems(context.getOrderItems());
        orderRepository.save(order);
        List<ProductSize> sizes = context.getOrderItems().stream()
                .map(OrderItem::getProductSize)
                .toList();
        productSizeService.updateStockAndSold(sizes);
        cartService.removeItemsFromCart(sizes.stream().map(ProductSize::getId).toList());
        context.getVouchers().forEach(voucher -> voucherService.decreaseVoucherQuantity(voucher.getId()));
        return order;
    }
}
