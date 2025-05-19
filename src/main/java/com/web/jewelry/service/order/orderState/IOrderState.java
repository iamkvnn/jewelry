package com.web.jewelry.service.order.orderState;

import com.web.jewelry.enums.EOrderStatus;

public interface IOrderState {
    void confirmOrder();
    void cancelOrder();
    void shipOrder();
    void deliverOrder();
    void returnOrder();
    void acceptReturn();
    void rejectReturn();
    void completeOrder();
    EOrderStatus getStatus();
}
