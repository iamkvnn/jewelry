package com.web.jewelry.service.payment;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.web.jewelry.config.MomoPaymentConfig;
import com.web.jewelry.dto.request.MomoPaymentRequest;
import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.MomoPayment;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.repository.MomoPaymentRepository;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MomoPaymentService implements IPaymentService {
    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final MomoPaymentRepository momoPaymentRepository;
    private final MomoPaymentConfig momoConfig;
    private final ModelMapper modelMapper;

    public String getPaymentUrl(String orderId) throws NoSuchAlgorithmException, InvalidKeyException {
        Order order = orderService.getOrder(orderId);
        if(order != null && order.getPaymentMethod().equals(EPaymentMethod.MOMO)){
            String returnUrl = "https://momo.vn";
            String notifyUrl = "https://ae92-14-169-5-215.ngrok-free.app/api/v1/payments/momo-callback";
            MomoPaymentRequest request = momoConfig.createPaymentRequest(orderId, order.getTotalPrice().toString(),
                    "Thanh toán đơn hàng " + orderId, returnUrl, notifyUrl, "", MomoPaymentConfig.ERequestType.PAY_WITH_ATM);

            String response = momoConfig.sendToMomo(request);
            if(response != null){
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                return jsonResponse.get("payUrl").getAsString();
            }
        }
        return null;
    }

    public void checkPayment(Map<String, Object> response){
        if(Objects.equals(response.get("resultCode"), 0) && momoConfig.isValidSignature(response)){
            System.out.println("ORDER RESPONSE: " + response.get("orderId"));
            String orderId = response.get("orderId").toString();
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            System.out.println("ORDER RESPONSE............");
            if(order != null && order.getPaymentMethod().equals(EPaymentMethod.MOMO)  && response.get("amount") != null
                    && (order.getTotalPrice() <= Long.parseLong(response.get("amount").toString()))){
                MomoPayment momoPayment = MomoPayment.builder()
                        .order(order)
                        .requestId(response.get("requestId").toString())
                        .paymentInfo(response.get("orderInfo").toString())
                        .paymentMessage(response.get("message").toString())
                        .amount(Long.parseLong(response.get("amount").toString()))
                        .paymentDate(LocalDateTime.now())
                        .transactionId(Long.parseLong(response.get("transId").toString()))
                        .resultCode(Integer.parseInt(response.get("resultCode").toString()))
                        .status(EPaymentStatus.PAID)
                        .build();
                System.out.println("MOMO PAYMENT: " + momoPayment);
                momoPaymentRepository.save(momoPayment);
            }
        }
    }

    @Override
    public Payment createPayment(String orderId){
        return null;
    }

    @Override
    public PaymentResponse convertToResponse(Payment payment) {
        return null;
    }
}
