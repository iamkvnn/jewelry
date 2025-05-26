package com.web.jewelry.service.payment;

import com.web.jewelry.enums.EPaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentServiceFactory {
    private final VNPayPaymentService vnPayPaymentService;
    private final MomoPaymentService momoPaymentService;
    private final CODPaymentService codPaymentService;

    public IPaymentStrategyService getPaymentService(EPaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case VN_PAY -> vnPayPaymentService;
            case MOMO -> momoPaymentService;
            case COD -> codPaymentService;
        };
    }
}
