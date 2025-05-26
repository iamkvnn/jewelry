package com.web.jewelry.service.payment;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.web.jewelry.config.MomoPaymentConfig;
import com.web.jewelry.dto.request.MomoPaymentRequest;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.MomoPayment;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.repository.MomoPaymentRepository;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.notification.INotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MomoPaymentService extends PaymentTemplate {
    private final MomoPaymentRepository momoPaymentRepository;
    private final MomoPaymentConfig momoConfig;

    @Value("${FE_BASE_URL}")
    private String feBaseUrl;

    public MomoPaymentService(INotificationService notificationService, OrderRepository orderRepository,
                              MomoPaymentRepository momoPaymentRepository, MomoPaymentConfig momoConfig) {
        super(notificationService, orderRepository);
        this.momoPaymentRepository = momoPaymentRepository;
        this.momoConfig = momoConfig;
    }

    @Override
    public MomoPayment createPayment(Order order) {
        MomoPayment payment = MomoPayment.builder()
                .order(order)
                .amount(order.getTotalPrice())
                .paymentMessage("Thanh toán đơn hàng " + order.getId())
                .status(EPaymentStatus.PROCESSING)
                .build();
        return momoPaymentRepository.save(payment);
    }

    @Override
    public String getPaymentUrl(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getPaymentMethod() != EPaymentMethod.MOMO) {
            throw new BadRequestException("Invalid payment method");
        }
        if (order.getMomoPayment().getStatus() != EPaymentStatus.PROCESSING) {
            throw new BadRequestException("Order has already been paid or cancelled");
        }
        String returnUrl = feBaseUrl + "/checkouts/thank-you/" + orderId;
        String notifyUrl = "https://api.shinyjewelry.shop/api/v1/payments/momo-callback";
        MomoPaymentRequest request;
        try {
            request = momoConfig.createPaymentRequest(orderId, order.getTotalPrice().toString(),
                        "Thanh toán đơn hàng " + orderId, returnUrl, notifyUrl, "", MomoPaymentConfig.ERequestType.PAY_WITH_ATM);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        String response = momoConfig.sendToMomo(request);
        if (response == null) {
            throw new RuntimeException("Failed to get payment URL");
        }
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse.get("payUrl").getAsString();
    }

    @Override
    protected void updatePayment(Payment payment) {
        momoPaymentRepository.save((MomoPayment) payment);
    }

    @Override
    public Payment validateCallback(Map<String, String> callbackData) {
        if(Objects.equals(callbackData.get("resultCode"), "0") && momoConfig.isValidSignature(callbackData)){
            String orderId = callbackData.get("orderId");
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            if(order != null && order.getPaymentMethod().equals(EPaymentMethod.MOMO) && callbackData.get("amount") != null
                    && (order.getTotalPrice() <= Long.parseLong(callbackData.get("amount")))){
                MomoPayment momoPayment = momoPaymentRepository.findByOrderId(orderId).orElseThrow(() -> new ResourceNotFoundException("Momo payment not found"));
                momoPayment.setStatus(EPaymentStatus.PAID);
                momoPayment.setPaymentDate(LocalDateTime.now());
                momoPayment.setRequestId(callbackData.get("requestId"));
                momoPayment.setTransactionId(Long.parseLong(callbackData.get("transId")));
                momoPayment.setResultCode(Integer.parseInt(callbackData.get("resultCode")));
                momoPayment.setPaymentInfo("Thanh toán thành công");
                return momoPayment;
            }
        }
        return null;
    }
}
