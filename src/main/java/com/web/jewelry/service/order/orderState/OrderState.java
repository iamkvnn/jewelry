package com.web.jewelry.service.order.orderState;

import com.web.jewelry.exception.BadRequestException;

public abstract class OrderState implements IOrderState {
    protected OrderStateContext context;

    public OrderState(OrderStateContext context) {
        this.context = context;
    }

    @Override
    public void confirmOrder() {
        throw new BadRequestException("Cannot confirm order in " + getStatus() + " state");
    }

    @Override
    public void cancelOrder() {
        throw new BadRequestException("Cannot cancel order in " + getStatus() + " state");
    }

    @Override
    public void shipOrder() {
        throw new BadRequestException("Cannot ship order in " + getStatus() + " state");
    }

    @Override
    public void deliverOrder() {
        throw new BadRequestException("Cannot deliver order in " + getStatus() + " state");
    }

    @Override
    public void returnOrder() {
        throw new BadRequestException("Cannot return order in " + getStatus() + " state");
    }

    @Override
    public void acceptReturn() {
        throw new BadRequestException("Cannot accept return in " + getStatus() + " state");
    }

    @Override
    public void rejectReturn() {
        throw new BadRequestException("Cannot reject return in " + getStatus() + " state");
    }

    @Override
    public void completeOrder() {
        throw new BadRequestException("Cannot complete order in " + getStatus() + " state");
    }
}