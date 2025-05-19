package com.web.jewelry.service.payment;

import com.web.jewelry.model.Payment;

import java.util.Map;

public interface IPaymentStrategyService {
    String getPaymentUrl(String orderId);
    Payment handleCallback(Map<String, String> callbackData);
    Payment validateCallback(Map<String, String> callbackData);
}
