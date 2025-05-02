package com.web.jewelry.service.payment;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.web.jewelry.config.MomoPaymentConfig;
import com.web.jewelry.dto.request.MomoPaymentRequest;
import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.MomoPayment;
import com.web.jewelry.model.Order;
import com.web.jewelry.repository.MomoPaymentRepository;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MomoPaymentService {
    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final MomoPaymentRepository momoPaymentRepository;
    private final MomoPaymentConfig momoConfig;
    private final INotificationService notificationService;
    //private final ModelMapper modelMapper;

    @Value("${FE_BASE_URL}")
    private String feBaseUrl;

    public String getPaymentUrl(String orderId) throws NoSuchAlgorithmException, InvalidKeyException {
        Order order = orderService.getOrder(orderId);
        if(order != null && order.getPaymentMethod().equals(EPaymentMethod.MOMO)){
            String returnUrl = feBaseUrl + "checkouts/thank-you?orderId=" + orderId;
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
        Locale localeVN = Locale.forLanguageTag("vi-VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        currencyFormatter.setCurrency(Currency.getInstance("VND"));
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
                notificationService.sendNotificationToSpecificCustomer(
                        NotificationRequest.builder()
                                .title("Thông báo đơn hàng")
                                .content("Đơn hàng " + orderId + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua Momo EWallet.")
                                .customerIds(Set.of(order.getCustomer().getId()))
                                .isEmail(true)
                                .build());
                notificationService.sendNotificationToAllStaff(
                        NotificationRequest.builder()
                                .title("Thông báo có đơn hàng mới")
                                .content("Đơn hàng " + orderId + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua Momo EWallet.\n Vui lòng kiểm tra.")
                                .build());
                notificationService.sendNotificationToAllManager(
                        NotificationRequest.builder()
                                .title("Vừa có đơn hàng mới được tạo")
                                .content("Đơn hàng " + orderId + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua Momo EWallet.")
                                .build());
            }
        }
    }
}
