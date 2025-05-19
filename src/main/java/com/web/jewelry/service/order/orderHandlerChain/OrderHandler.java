package com.web.jewelry.service.order.orderHandlerChain;

import com.web.jewelry.model.Order;

public abstract class OrderHandler {
    private OrderHandler nextHandler;

    public void setNext(OrderHandler next) {
        this.nextHandler = next;
    }

    public abstract Order process(OrderHandlerContext context);

    protected Order processNext(OrderHandlerContext context) {
        if (nextHandler != null) {
            return nextHandler.process(context);
        }
        return context.getOrder();
    }
}
