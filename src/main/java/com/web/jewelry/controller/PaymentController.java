package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.PaymentResponse;
import com.web.jewelry.model.Payment;
import com.web.jewelry.service.payment.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/payments")
public class PaymentController {
    private final IPaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPayment(@RequestParam Long orderId) {
        Payment payment = paymentService.createPayment(orderId);
        PaymentResponse paymentResponse = paymentService.convertToResponse(payment);
        return ResponseEntity.ok(new ApiResponse("200", "Success", paymentResponse));
    }
}
