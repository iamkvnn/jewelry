package com.web.jewelry.controller;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.VoucherRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.VoucherResponse;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.model.Voucher;
import com.web.jewelry.service.voucher.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/vouchers")
public class VoucherController {
    private final IVoucherService voucherService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllVouchers() {
        List<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.getAllVouchers());
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchVouchers(@RequestParam String query) {
        List<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.searchVouchers(query));
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/valid")
    public ResponseEntity<ApiResponse> getValidVouchers() {
        List<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.getValidVouchers());
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/type")
    public ResponseEntity<ApiResponse> getVoucherByType(@RequestParam EVoucherType type) {
        List<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.getVoucherByType(type));
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/code")
    public ResponseEntity<ApiResponse> getVoucherByCode(@RequestParam String code) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.getVoucherByCode(code));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getVoucherById(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.getVoucherById(id));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @PostMapping("/check-validate")
    public ResponseEntity<ApiResponse> validateVoucher(@RequestBody OrderRequest request) {
        List<Voucher> validatedVouchers = voucherService.validateVouchers(request);
        List<VoucherResponse> responses = voucherService.convertToResponse(validatedVouchers);
        return ResponseEntity.ok(new ApiResponse("200", "Success", responses));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addVoucher(@RequestBody VoucherRequest request) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.addVoucher(request));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateVoucher(@PathVariable Long id, @RequestBody VoucherRequest request) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.updateVoucher(id, request));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }


}
