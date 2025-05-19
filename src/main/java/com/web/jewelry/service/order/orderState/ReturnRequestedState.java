package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class ReturnRequestedState extends OrderState {
    public ReturnRequestedState(OrderStateContext context) {
        super(context);
    }

    @Override
    public void acceptReturn() {
        context.setOrderState(new ReturnedState(context));
    }

    @Override
    public void rejectReturn() {
        context.setOrderState(new ReturnRejectedState(context));
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.RETURN_REQUESTED;
    }
}
