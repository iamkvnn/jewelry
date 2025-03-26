package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.model.Payment;
import com.web.jewelry.service.payment.CODPaymentService;
import com.web.jewelry.service.payment.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/payments")
public class PaymentController {
    // XH moi doi thanh codPaymentService, chua test chay duoc ko
    private final CODPaymentService codPaymentService;
    private final MomoPaymentService momoPaymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPayment(@RequestParam Long orderId) {
        Payment payment = codPaymentService.createPayment(orderId);
        PaymentResponse paymentResponse = codPaymentService.convertToResponse(payment);
        return ResponseEntity.ok(new ApiResponse("200", "Success", paymentResponse));
    }
    @PostMapping("/momo-payment")
    public ResponseEntity<ApiResponse> requestMomoPayment(@RequestParam Long orderId) throws NoSuchAlgorithmException, InvalidKeyException {
        String response = momoPaymentService.getPaymentUrl(orderId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }
    @PostMapping("/momo-callback")
    public ResponseEntity<ApiResponse> handleMoMoCallback(@RequestBody Map<String, Object> response) {
        System.out.println("MoMo Response: " + response);
        if(momoPaymentService.checkPayment(response))
            return ResponseEntity.ok(new ApiResponse("200", "Success", "Payment successful"));
        else
            return ResponseEntity.ok(new ApiResponse("200", "Success", "Payment failed"));
    }
}
