package com.web.jewelry.service.order.orderHandlerChain;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.OrderItem;
import com.web.jewelry.model.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderValidationHandler extends OrderHandler {
    @Override
    public Order process(OrderHandlerContext context) {
        OrderRequest orderRequest = context.getOrderRequest();
        List<OrderItem> orderItems = context.getOrderItems();
        List<Voucher> vouchers = context.getVouchers();
        Long total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(0L, Long::sum);
        if (!total.equals(orderRequest.getTotalProductPrice())) {
            throw new BadRequestException("Total product price is not correct");
        }
        vouchers.forEach(voucher -> {
            if (voucher.getType() == EVoucherType.FREESHIP) {
                Long discount = voucher.getDiscountRate() * orderRequest.getShippingFee() > voucher.getApplyLimit() ?
                        voucher.getApplyLimit() : voucher.getDiscountRate() * orderRequest.getShippingFee() / 100;
                if (!discount.equals(orderRequest.getFreeShipDiscount())) {
                    throw new BadRequestException("Free ship discount is not correct");
                }
            } else if (voucher.getType() == EVoucherType.PROMOTION) {
                Long discount = voucher.getDiscountRate() * orderRequest.getTotalProductPrice() > voucher.getApplyLimit() ?
                        voucher.getApplyLimit() : voucher.getDiscountRate() * orderRequest.getTotalProductPrice() / 100;
                if (!discount.equals(orderRequest.getPromotionDiscount())) {
                    throw new BadRequestException("Promotion discount is not correct");
                }
            }
        });
        if (!orderRequest.getTotalPrice().equals(total + orderRequest.getShippingFee() - orderRequest.getFreeShipDiscount() - orderRequest.getPromotionDiscount())) {
            throw new BadRequestException("Total price is not correct");
        }
        return processNext(context);
    }
}
