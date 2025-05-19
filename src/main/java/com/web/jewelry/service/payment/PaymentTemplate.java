package com.web.jewelry.service.payment;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.Payment;
import com.web.jewelry.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public abstract class PaymentTemplate implements IPaymentStrategyService{
    private final INotificationService notificationService;

    @Override
    public abstract String getPaymentUrl(String orderId);

    @Override
    public Payment handleCallback(Map<String, String> callbackData){
        return validateCallback(callbackData);
    }

    @Override
    public abstract Payment validateCallback(Map<String, String> callbackData);

    protected abstract void updatePayment(Payment payment);

    protected void sendNotification(Order order) {
        Locale localeVN = Locale.forLanguageTag("vi-VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        currencyFormatter.setCurrency(Currency.getInstance("VND"));
        notificationService.sendNotificationToSpecificCustomer(
                NotificationRequest.builder()
                        .title("Thông báo đơn hàng")
                        .content("Đơn hàng " + order.getId() + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua VNPay.")
                        .customerIds(Set.of(order.getCustomer().getId()))
                        .isEmail(true)
                        .build());
        notificationService.sendNotificationToAllStaff(
                NotificationRequest.builder()
                        .title("Thông báo có đơn hàng mới")
                        .content("Đơn hàng " + order.getId() + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua VNPay.\n Vui lòng kiểm tra.")
                        .build());
        notificationService.sendNotificationToAllManager(
                NotificationRequest.builder()
                        .title("Vừa có đơn hàng mới được tạo")
                        .content("Đơn hàng " + order.getId() + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua VNPay.")
                        .build());
    }

    public void processCallback(Map<String, String> callbackData) {
        Payment payment = handleCallback(callbackData);
        updatePayment(payment);
        //Order order = payment.getOrder();
        //sendNotification(order);
    }

    protected abstract Payment createPayment(Order order);
}
