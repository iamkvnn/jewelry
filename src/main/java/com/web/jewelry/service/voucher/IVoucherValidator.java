package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.model.Voucher;

public interface VoucherValidator {
    boolean isValid(Voucher voucher, OrderRequest request);
    String getErrorMessage(Voucher voucher, OrderRequest request);
}
