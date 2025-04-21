package com.web.jewelry.service.payment;

import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.model.Payment;

import java.security.InvalidKeyException;

public interface IPaymentService {
    Payment createPayment(String orderId) throws InvalidKeyException;
    PaymentResponse convertToResponse(Payment payment);
}
