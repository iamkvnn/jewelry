package com.web.jewelry.controller;

import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.VNPayIPNResponse;
import com.web.jewelry.service.payment.MomoPaymentService;
import com.web.jewelry.service.payment.VNPayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final MomoPaymentService momoPaymentService;
    private final VNPayPaymentService vnPayPaymentService;

    @PostMapping("/momo-payment")
    public ResponseEntity<ApiResponse> requestMomoPayment(@RequestParam String orderId) throws NoSuchAlgorithmException, InvalidKeyException {
        String response = momoPaymentService.getPaymentUrl(orderId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }
    @PostMapping("/momo-callback")
    public ResponseEntity<ApiResponse> handleMoMoCallback(@RequestBody Map<String, Object> response) {
        momoPaymentService.checkPayment(response) ;
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/vnpay-payment")
    public ResponseEntity<ApiResponse> requestVNPayPayment(@RequestParam String orderId) throws NoSuchAlgorithmException, InvalidKeyException {
        String response = vnPayPaymentService.getPaymentUrl(orderId);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("/IPN")
    public ResponseEntity<VNPayIPNResponse> handleVNPayIPN(HttpServletRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
        if (vnPayPaymentService.checkPayment(request)) {
            return ResponseEntity.ok(new VNPayIPNResponse("00", "Success"));
        } else {
            return ResponseEntity.ok(new VNPayIPNResponse("99", "Failed"));
        }
    }
}
