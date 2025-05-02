package com.web.jewelry.controller;

import com.web.jewelry.dto.request.PrivacyAndTermRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.PrivacyAndTermResponse;
import com.web.jewelry.model.PrivacyAndTerm;
import com.web.jewelry.service.privacy.IPrivacyAndTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/privacy-and-terms")
public class PrivacyAndTermController {
    private final IPrivacyAndTermService privacyAndTermService;

    @GetMapping("/get")
    public ResponseEntity<ApiResponse> get() {
        PrivacyAndTerm privacyAndTerm = privacyAndTermService.get();
        PrivacyAndTermResponse response = privacyAndTermService.convertToResponse(privacyAndTerm);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> update(@RequestBody PrivacyAndTermRequest request) {
        PrivacyAndTerm privacyAndTerm = privacyAndTermService.update(request);
        PrivacyAndTermResponse response = privacyAndTermService.convertToResponse(privacyAndTerm);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }
}
