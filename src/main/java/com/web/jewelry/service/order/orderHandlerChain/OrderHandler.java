package com.web.jewelry.service.order;

import com.web.jewelry.model.Order;

public abstract class OrderHandler {
    private OrderHandler nextHandler;

    public void setNext(OrderHandler next) {
        this.nextHandler = next;
    }

    public abstract Order process(OrderContext context);

    protected Order processNext(OrderContext context) {
        if (nextHandler != null) {
            return nextHandler.process(context);
        }
        return context.getOrder();
    }
}
