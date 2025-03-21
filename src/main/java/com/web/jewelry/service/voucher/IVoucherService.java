package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.dto.request.VoucherRequest;
import com.web.jewelry.dto.response.VoucherResponse;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.model.Voucher;

import java.util.List;


public interface IVoucherService {
    List<Voucher> getAllVouchers();
    Voucher getVoucherById(Long id);
    Voucher getVoucherByCode(String code);
    List<Voucher> getVoucherByType(EVoucherType type);
    List<Voucher> getValidVouchers();
    Voucher addVoucher(VoucherRequest request);
    Voucher updateVoucher(Long id, VoucherRequest request);
    void deleteVoucher(Long id);
    List<Voucher> validateVouchers(OrderRequest request);
    VoucherResponse convertToResponse(Voucher voucher);
    List<VoucherResponse> convertToResponse(List<Voucher> vouchers);
}
