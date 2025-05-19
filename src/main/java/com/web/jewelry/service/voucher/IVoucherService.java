package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.VoucherRequest;
import com.web.jewelry.dto.response.VoucherResponse;
import com.web.jewelry.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface IVoucherService {
    Page<Voucher> getAllVouchers(Pageable pageable);
    Page<Voucher> searchVouchers(String query, Pageable pageable);
    Voucher getVoucherById(Long id);
    Voucher getVoucherByCode(String code);
    Page<Voucher> getValidVouchers(Pageable pageable);
    Voucher addVoucher(VoucherRequest request);
    Voucher updateVoucher(Long id, VoucherRequest request);
    void deleteVoucher(Long id);
    List<Voucher> validateVouchers(OrderRequest request);
    VoucherResponse convertToResponse(Voucher voucher);
    Page<VoucherResponse> convertToResponse(Page<Voucher> vouchers);
    Page<Voucher> getValidVouchersForOrder(OrderRequest request, Pageable pageable);
    void decreaseVoucherQuantity(Long id);
}
