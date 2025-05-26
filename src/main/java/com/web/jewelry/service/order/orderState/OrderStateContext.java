package com.web.jewelry.service.order.orderState;

import com.web.jewelry.model.Order;

public class OrderStateContext {
    private IOrderState orderState;
    private final Order order;

    public OrderStateContext(Order order) {
        this.order = order;
        this.orderState = switch (order.getStatus()) {
            case PENDING -> new PendingState(this);
            case CONFIRMED -> new ConfirmedState(this);
            case CANCELLED -> new CancelledState(this);
            case SHIPPING -> new ShippingState(this);
            case DELIVERED -> new DeliveredState(this);
            case RETURNED -> new ReturnedState(this);
            case COMPLETED -> new CompletedState(this);
            case RETURN_REQUESTED -> new ReturnRequestedState(this);
            case RETURN_REJECTED -> new ReturnRejectedState(this);
        };
    }

    public void setOrderState(IOrderState orderState) {
        this.orderState = orderState;
        this.order.setStatus(orderState.getStatus());
    }

    public void confirmOrder() {
        orderState.confirmOrder();
    }

    public void cancelOrder() {
        orderState.cancelOrder();
    }

    public void shipOrder() {
        orderState.shipOrder();
    }

    public void deliverOrder() {
        orderState.deliverOrder();
    }

    public void returnOrder() {
        orderState.returnOrder();
    }

    public void acceptReturn() {
        orderState.acceptReturn();
    }

    public void rejectReturn() {
        orderState.rejectReturn();
    }

    public void completeOrder() {
        orderState.completeOrder();
    }
}
