package com.web.jewelry.controller;

import com.nimbusds.jose.JOSEException;
import com.web.jewelry.dto.request.*;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.AuthenticationResponse;
import com.web.jewelry.dto.response.IntrospectResponse;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.User;
import com.web.jewelry.service.authentication.AuthenticationService;
import com.web.jewelry.service.authentication.EmailService;
import com.web.jewelry.service.cart.ICartService;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
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
    private final EmailService emailService;
    private final Map<String, String> verificationCodes = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> addUser(@RequestBody UserRequest request) {
        User user = userService.createCustomer(request);
        cartService.initializeNewCart((Customer) user);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse> authenticate(@RequestBody AuthenticationRequest request, @RequestParam String role) {
        AuthenticationResponse response = authenticationService.authenticate(request, role);
        return ResponseEntity.ok(new ApiResponse("200",response.isAuthenticated() ? "Success" : "Failed", response));
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
        String code = emailService.sendVerificationEmail(email);

        long expiresAt = System.currentTimeMillis() + 60 * 1000;
        verificationCodes.put(email, code);
        scheduleCodeExpiration(email);

        Map<String, Object> response = new HashMap<>();
        response.put("expiresAt", expiresAt);

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
    private void scheduleCodeExpiration(String email) {
        scheduler.schedule(() -> verificationCodes.remove(email), 60, TimeUnit.SECONDS);
    }
}
