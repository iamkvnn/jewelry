package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class ReturnedState extends OrderState {
    public ReturnedState(OrderStateContext context) {
        super(context);
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.RETURNED;
    }
}
