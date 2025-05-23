package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.VNPayIPNResponse;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.service.payment.PaymentServiceFactory;
import com.web.jewelry.service.payment.adapter.VNPayCallbackAdapter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/payments")
public class PaymentController {
    private final PaymentServiceFactory factory;

    @PostMapping("/momo-payment")
    public ResponseEntity<ApiResponse> requestMomoPayment(@RequestParam String orderId) {
        String response = factory.getPaymentService(EPaymentMethod.MOMO).getPaymentUrl(orderId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }
    @PostMapping("/momo-callback")
    public ResponseEntity<ApiResponse> handleMoMoCallback(@RequestBody Map<String, String> response) {
        factory.getPaymentService(EPaymentMethod.MOMO).handleCallback(response);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/vnpay-payment")
    public ResponseEntity<ApiResponse> requestVNPayPayment(@RequestParam String orderId) {
        String response = factory.getPaymentService(EPaymentMethod.VN_PAY).getPaymentUrl(orderId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("/IPN")
    public ResponseEntity<VNPayIPNResponse> handleVNPayIPN(HttpServletRequest request) {
        if (factory.getPaymentService(EPaymentMethod.VN_PAY).handleCallback(new VNPayCallbackAdapter(request))) {
            return ResponseEntity.ok(new VNPayIPNResponse("00", "Success"));
        } else {
            return ResponseEntity.ok(new VNPayIPNResponse("99", "Failed"));
        }
    }
}
