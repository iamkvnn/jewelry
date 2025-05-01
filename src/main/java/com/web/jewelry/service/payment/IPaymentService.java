package com.web.jewelry.service.payment;

import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.model.Payment;

public interface IPaymentService {
    PaymentResponse convertToResponse(Payment payment);
}
