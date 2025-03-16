package com.web.jewelry.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.web.jewelry.dto.request.AuthenticationRequest;
import com.web.jewelry.dto.request.CollectionRequest;
import com.web.jewelry.dto.request.IntrospectRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.AuthenticationResponse;
import com.web.jewelry.dto.response.IntrospectResponse;
import com.web.jewelry.service.Authentication.AuthenticationService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

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

}
