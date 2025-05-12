package com.web.jewelry.controller;

import com.nimbusds.jose.JOSEException;
import com.web.jewelry.dto.request.*;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.AuthenticationResponse;
import com.web.jewelry.dto.response.IntrospectResponse;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.User;
import com.web.jewelry.service.authentication.AuthenticationService;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.email.EmailQueueService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final IUserService userService;
    private final ICartService cartService;
    private final EmailQueueService emailQueueService;
    private final Map<String, String> verificationCodes = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> addUser(@RequestBody UserRequest request) {
        User user = userService.createCustomer(request);
        cartService.initializeNewCart((Customer) user);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/call-back/google")
    public ResponseEntity<ApiResponse> googleCallBack(@RequestParam String code, @RequestParam EUserRole role) throws IOException {
        Customer cus = authenticationService.verifyIDTokenAndGetUser(code);
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail(cus.getEmail());
        request.setRole(role);
        request.setLoginWithGoogle(true);
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/introspect")
    ResponseEntity<ApiResponse> authenticate (@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        IntrospectResponse introspectResponse = authenticationService.introspect(request);
        return ResponseEntity.ok(new ApiResponse("200", introspectResponse.isValid() ? "Success" : "Failed", introspectResponse));
    }

    @PostMapping("/send-email")
    public ResponseEntity<ApiResponse> sendEmail(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String code = generateVerificationCode();
        emailQueueService.enqueue(new EmailRequest(email, "Xác nhận đăng ký tài khoản", "Mã xác nhận của bạn là: " + code));
        long expiredAt = System.currentTimeMillis() + 60 * 1000;
        verificationCodes.put(email, code);
        scheduleCodeExpiration(email, code);

        Map<String, Object> response = new HashMap<>();
        response.put("expiredAt", expiredAt);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyCode(@RequestBody VerifyEmailRequest request) {
        if (verificationCodes.containsKey(request.getEmail()) && verificationCodes.get(request.getEmail()).equals(request.getCode())) {
            verificationCodes.remove(request.getEmail());
            return ResponseEntity.ok(new ApiResponse("200", "Success", "Verify code successful"));
        }
        return ResponseEntity.ok(new ApiResponse("1001", "Failed", "Verify code failed"));
    }

    private void scheduleCodeExpiration(String email, String code) {
        scheduler.schedule(() -> {
            if (verificationCodes.containsKey(email) && verificationCodes.get(email).equals(code)) {
                verificationCodes.remove(email);
            }
        }, 60, TimeUnit.SECONDS);
    }

    @PostMapping("/reset-password/send-email")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String email, @RequestParam EUserRole role) {
        userService.sendEmailResetPassword(email, role);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PostMapping("/reset-password/verify")
    public ResponseEntity<ApiResponse> verifyResetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/confirm-delete-my-account")
    public ResponseEntity<ApiResponse> confirmDeleteCurrentCustomer(@RequestParam String token) {
        userService.confirmDeleteCurrentCustomer(token);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
}
