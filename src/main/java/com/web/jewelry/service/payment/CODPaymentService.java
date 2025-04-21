package com.web.jewelry.service.payment;

import com.web.jewelry.dto.request.MomoPaymentRequest;
import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.model.CODPayment;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.repository.CODPaymentRepository;
import com.web.jewelry.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CODPaymentService implements IPaymentService {
    private final CODPaymentRepository codPaymentRepository;
    private final OrderService orderService;
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
            return codPaymentRepository.save(payment);
        }
        return null;
    }

    @Override
    public PaymentResponse convertToResponse(Payment payment) {
        return modelMapper.map(payment, PaymentResponse.class);
    }
}
