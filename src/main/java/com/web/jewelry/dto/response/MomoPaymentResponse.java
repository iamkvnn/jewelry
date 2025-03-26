package com.web.jewelry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentResponse {
    private Long transactionId;
    private String partnerCode;
    private String requestId;
    private Long amount;
    private String responseTime;
    private String message;
    private int resultCode;
    private String signature;
}
