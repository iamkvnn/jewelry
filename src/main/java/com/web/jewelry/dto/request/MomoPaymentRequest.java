package com.web.jewelry.dto.request;

import com.web.jewelry.config.MomoPaymentConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MomoPaymentRequest {
    // Thông tin đối tacs momo
    String partnerCode;
    String accessKey;
    // Thong tin don hang
    String requestId;
    String amount;
    String orderId;
    String orderInfo;
    String returnUrl;
    String notifyUrl;
    String extraData;
    MomoPaymentConfig.ERequestType requestType;
    String signature;

    public MomoPaymentRequest(String partnerCode, String accessKey, String requestId, String amount,
                              String orderId, String orderInfo, String returnUrl, String notifyUrl,
                              String extraData, MomoPaymentConfig.ERequestType requestType, String signature) {
        this.partnerCode = partnerCode;
        this.accessKey = accessKey;
        this.requestId = requestId;
        this.amount = amount;
        this.orderId = orderId;
        this.orderInfo = orderInfo;
        this.returnUrl = returnUrl;
        this.notifyUrl = notifyUrl;
        this.extraData = extraData;
        this.requestType = requestType;
        this.signature = signature;
    }
}