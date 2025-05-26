package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class CancelledState extends OrderState {
    public CancelledState(OrderStateContext context) {
        super(context);
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.CANCELLED;
    }
}
