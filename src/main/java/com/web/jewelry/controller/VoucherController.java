package com.web.jewelry.controller;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.VoucherRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.VoucherResponse;
import com.web.jewelry.service.voucher.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/vouchers")
public class VoucherController {
    private final IVoucherService voucherService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllVouchers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.getAllVouchers(PageRequest.of(page - 1, size)));
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchVouchers(@RequestParam String query, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.searchVouchers(query, PageRequest.of(page - 1, size)));
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/valid")
    public ResponseEntity<ApiResponse> getValidVouchers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.getValidVouchers(PageRequest.of(page - 1, size)));
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @PostMapping("/valid-for-order")
    public ResponseEntity<ApiResponse> getValidVouchersForOrder(@RequestBody OrderRequest request, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "30") int size) {
        Page<VoucherResponse> vouchers = voucherService.convertToResponse(voucherService.getValidVouchersForOrder(request, PageRequest.of(page - 1, size)));
        return ResponseEntity.ok(new ApiResponse("200", "Success", vouchers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getVoucherById(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.getVoucherById(id));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @PostMapping("/check-validate")
    public ResponseEntity<ApiResponse> validateVoucher(@RequestBody OrderRequest request) {
        voucherService.validateVouchers(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addVoucher(@RequestBody VoucherRequest request) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.addVoucher(request));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateVoucher(@PathVariable Long id, @RequestBody VoucherRequest request) {
        VoucherResponse voucher = voucherService.convertToResponse(voucherService.updateVoucher(id, request));
        return ResponseEntity.ok(new ApiResponse("200", "Success", voucher));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
