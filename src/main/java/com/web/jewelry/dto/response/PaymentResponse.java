package com.web.jewelry.dto.response;

import com.web.jewelry.enums.EPaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    protected Long id;
    protected Long amount;
    protected EPaymentStatus status;
    protected LocalDateTime paymentDate;
    protected String paymentInfo;
    protected String paymentMessage;
}
