package com.web.jewelry.service.payment;

import com.web.jewelry.config.VNPayPaymentConfig;
import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.enums.EPaymentStatus;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Order;
import com.web.jewelry.model.VNPayPayment;
import com.web.jewelry.repository.OrderRepository;
import com.web.jewelry.repository.VNPayPaymentRepository;
import com.web.jewelry.service.notification.INotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class VNPayPaymentService{
    private final OrderRepository orderRepository;
    private final VNPayPaymentRepository vnPayPaymentRepository;
    private final VNPayPaymentConfig vnPayConfig;
    private final INotificationService notificationService;
    //private final ModelMapper modelMapper;

    @Value("${VNPay.vnp_Version}")
    private String vnp_Version;
    @Value("${VNPay.vnp_Command}")
    private String vnp_Command;
    @Value("${VNPay.vnp_TmnCode}")
    private String vnp_TmnCode;
    @Value("${VNPay.vnp_Url}")
    private String vnp_Url;
    @Value("${FE_BASE_URL}")
    private String feBaseUrl;

    public VNPayPayment createPayment(Order order) {
        VNPayPayment payment = VNPayPayment.builder()
                .order(order)
                .amount(order.getTotalPrice())
                .paymentMessage("Thanh toán đơn hàng " + order.getId())
                .paymentInfo("Thanh toán khi nhận hàng")
                .status(EPaymentStatus.PROCESSING)
                .build();
        return vnPayPaymentRepository.save(payment);
    }

    public String getPaymentUrl(String orderId) throws NoSuchAlgorithmException, InvalidKeyException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
        long vnp_Amount = order.getTotalPrice() * 100;
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(vnp_Amount));
        params.put("vnp_BankCode", "VNBANK");
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", "103.149.252.125");
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        String orderType = "other";
        params.put("vnp_OrderType", orderType);
        params.put("vnp_ReturnUrl", feBaseUrl + "/checkouts/thank-you/" + orderId);
        params.put("vnp_ExpireDate", expireDate);
        params.put("vnp_TxnRef", vnp_TxnRef);
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnp_Url + "?" + queryUrl;
    }

    public boolean checkPayment(HttpServletRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
        Locale localeVN = Locale.forLanguageTag("vi-VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        currencyFormatter.setCurrency(Currency.getInstance("VND"));
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName;
            String fieldValue;
            fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = vnPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash) && request.getParameter("vnp_ResponseCode").equals("00")) {
            String orderId = request.getParameter("vnp_OrderInfo").substring(20);
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            VNPayPayment payment = vnPayPaymentRepository.findByOrderId(order.getId()).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
            payment.setTransactionNumber(request.getParameter("vnp_TransactionNo"));
            payment.setBank(request.getParameter("vnp_BankCode"));
            payment.setVnPayResponseCode(request.getParameter("vnp_ResponseCode"));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus(EPaymentStatus.PAID);
            vnPayPaymentRepository.save(payment);
            notificationService.sendNotificationToSpecificCustomer(
                    NotificationRequest.builder()
                            .title("Thông báo đơn hàng")
                            .content("Đơn hàng " + orderId + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua VNPay.")
                            .customerIds(Set.of(order.getCustomer().getId()))
                            .isEmail(true)
                            .build());
            notificationService.sendNotificationToAllStaff(
                    NotificationRequest.builder()
                            .title("Thông báo có đơn hàng mới")
                            .content("Đơn hàng " + orderId + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua VNPay.\n Vui lòng kiểm tra.")
                            .build());
            notificationService.sendNotificationToAllManager(
                    NotificationRequest.builder()
                            .title("Vừa có đơn hàng mới được tạo")
                            .content("Đơn hàng " + orderId + " đã được đặt và thanh toán thành công số tiền " + currencyFormatter.format(order.getTotalPrice()) + " qua VNPay.")
                            .build());
        }
        return false;
    }
}
