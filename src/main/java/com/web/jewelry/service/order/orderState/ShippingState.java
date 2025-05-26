package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class ShippingState extends OrderState {
    public ShippingState(OrderStateContext context) {
        super(context);
    }

    @Override
    public void deliverOrder() {
        context.setOrderState(new DeliveredState(context));
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.SHIPPING;
    }
}
