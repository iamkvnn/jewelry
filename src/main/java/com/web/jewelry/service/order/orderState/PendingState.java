package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class PendingState extends OrderState {
    public PendingState(OrderStateContext context) {
        super(context);
    }

    @Override
    public void confirmOrder() {
        context.setOrderState(new ConfirmedState(context));
    }

    @Override
    public void cancelOrder() {
        context.setOrderState(new CancelledState(context));
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.PENDING;
    }
}
