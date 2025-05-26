package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class ConfirmedState extends OrderState {
    public ConfirmedState(OrderStateContext context) {
        super(context);
    }

    @Override
    public void shipOrder() {
        context.setOrderState(new ShippingState(context));
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.CONFIRMED;
    }
}
