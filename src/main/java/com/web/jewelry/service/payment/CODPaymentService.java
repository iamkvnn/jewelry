package com.web.jewelry.service.payment;

import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.model.CODPayment;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.repository.CODPaymentRepository;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.notification.INotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CODPaymentService extends PaymentTemplate {
    private final CODPaymentRepository codPaymentRepository;

    public CODPaymentService(INotificationService notificationService, CODPaymentRepository codPaymentRepository, OrderRepository orderRepository) {
        super(notificationService, orderRepository);
        this.codPaymentRepository = codPaymentRepository;
    }

    public CODPayment createPayment(Order order) {
        CODPayment payment = CODPayment.builder()
                .order(order)
                .amount(order.getTotalPrice())
                .paymentMessage("Thanh toán đơn hàng " + order.getId())
                .paymentInfo("Thanh toán khi nhận hàng")
                .status(EPaymentStatus.PROCESSING)
                .paymentDate(LocalDateTime.now())
                .build();
        sendNotification(order);
        return codPaymentRepository.save(payment);
    }

    @Override
    public boolean handleCallback(Map<String, String> callbackData) {
        return false;
    }

    @Override
    public String getPaymentUrl(String orderId) {
        return "";
    }

    @Override
    public Payment validateCallback(Map<String, String> callbackData) {
        return null;
    }

    @Override
    protected void updatePayment(Payment payment) {

    }
}
