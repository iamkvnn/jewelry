package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public class ReturnRejectedState extends OrderState {
    public ReturnRejectedState(OrderStateContext context) {
        super(context);
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.RETURN_REJECTED;
    }
}
