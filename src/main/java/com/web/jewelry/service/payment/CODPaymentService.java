package com.web.jewelry.service.payment;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.model.CODPayment;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.repository.CODPaymentRepository;
import com.web.jewelry.service.notification.INotificationService;
import com.web.jewelry.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CODPaymentService implements IPaymentService {
    private final CODPaymentRepository codPaymentRepository;
    private final OrderService orderService;
    private final INotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    public Payment createPayment(String orderId) {
        Order order = orderService.getOrder(orderId);
        if (order.getPaymentMethod().equals(EPaymentMethod.COD)) {
            CODPayment payment = CODPayment.builder()
                    .order(order)
                    .amount(order.getTotalPrice())
                    .paymentMessage("Thanh toán đơn hàng " + order.getId())
                    .paymentInfo("Thanh toán khi nhận hàng")
                    .status(EPaymentStatus.PROCESSING)
                    .paymentDate(LocalDateTime.now())
                    .build();
            notificationService.sendNotificationToSpecificCustomer(
                    NotificationRequest.builder()
                            .title("Thông báo đơn hàng")
                            .content("Đơn hàng " + order.getId() + " đã được tạo với phương thức thanh toán COD.")
                            .customerIds(Set.of(order.getCustomer().getId()))
                            .isEmail(true)
                            .build());
            notificationService.sendNotificationToAllStaff(
                    NotificationRequest.builder()
                            .title("Thông báo có đơn hàng mới")
                            .content("Đơn hàng " + order.getId() + " đã được tạo với phương thức thanh toán COD vui lòng kiểm tra.")
                            .build());
            notificationService.sendNotificationToAllManager(
                    NotificationRequest.builder()
                            .title("Vừa có đơn hàng mới được tạo")
                            .content("Đơn hàng " + order.getId() + " đã được tạo với phương thức thanh toán COD.")
                            .build());
            return codPaymentRepository.save(payment);
        }
        return null;
    }

    @Override
    public PaymentResponse convertToResponse(Payment payment) {
        return modelMapper.map(payment, PaymentResponse.class);
    }
}
