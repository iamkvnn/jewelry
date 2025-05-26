package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class CompletedState extends OrderState{
    public CompletedState(OrderStateContext context) {
        super(context);
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.COMPLETED;
    }
}
