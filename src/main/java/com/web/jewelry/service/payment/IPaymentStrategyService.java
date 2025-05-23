package com.web.jewelry.service.payment;

import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;

import java.util.Map;

public interface IPaymentStrategyService {
    Payment createPayment(Order order);
    String getPaymentUrl(String orderId);
    boolean handleCallback(Map<String, String> callbackData);
    Payment validateCallback(Map<String, String> callbackData);
}
