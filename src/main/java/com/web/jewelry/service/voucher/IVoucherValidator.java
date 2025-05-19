package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.model.Voucher;

import java.util.List;

public interface IVoucherValidator {
    boolean isValid(List<Voucher> vouchers, OrderRequest request);
}
