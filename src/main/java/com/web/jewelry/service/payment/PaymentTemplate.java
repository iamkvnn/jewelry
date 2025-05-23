package com.web.jewelry.service.payment;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public abstract class PaymentTemplate implements IPaymentStrategyService{
    protected final INotificationService notificationService;
    protected final OrderRepository orderRepository;

    @Override
    public boolean handleCallback(Map<String, String> callbackData){
        Payment payment = validateCallback(callbackData);
        if (payment == null) {
            return false;
        }
        String orderId = callbackData.get("orderId");
        if (orderId == null) {
            orderId = callbackData.get("vnp_OrderInfo").substring(20);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        updatePayment(payment);
        sendNotification(order);
        return true;
    }

    protected abstract void updatePayment(Payment payment);

    protected void sendNotification(Order order) {
        Locale localeVN = Locale.forLanguageTag("vi-VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        currencyFormatter.setCurrency(Currency.getInstance("VND"));
        notificationService.sendNotificationToSpecificCustomer(
                NotificationRequest.builder()
                        .title("Thông báo đơn hàng")
                        .content("Đơn hàng " + order.getId() + " đã được đặt thành công với số tiền " + currencyFormatter.format(order.getTotalPrice()))
                        .customerIds(Set.of(order.getCustomer().getId()))
                        .isEmail(true)
                        .build());
        notificationService.sendNotificationToAllStaff(
                NotificationRequest.builder()
                        .title("Thông báo có đơn hàng mới")
                        .content("Đơn hàng " + order.getId() + " đã được đặt thành công với số tiền " + currencyFormatter.format(order.getTotalPrice()) + ".\n Vui lòng kiểm tra.")
                        .build());
        notificationService.sendNotificationToAllManager(
                NotificationRequest.builder()
                        .title("Vừa có đơn hàng mới được tạo")
                        .content("Đơn hàng " + order.getId() + " đã được đặt thành công với số tiền " + currencyFormatter.format(order.getTotalPrice()))
                        .build());
    }
}
