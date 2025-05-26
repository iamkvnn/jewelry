package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.model.Customer;

public class DeliveredState extends OrderState{
    public DeliveredState(OrderStateContext context) {
        super(context);
    }

    public void completeOrder() {
        context.setOrderState(new CompletedState(context));
    }

    public void returnOrder() {
        context.setOrderState(new ReturnedState(context));
    }

    @Override
    public EOrderStatus getStatus() {
        return EOrderStatus.DELIVERED;
    }
}
