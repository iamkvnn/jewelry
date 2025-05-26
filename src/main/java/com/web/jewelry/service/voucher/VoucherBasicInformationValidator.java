package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.model.Voucher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class VoucherBasicInformationValidator implements IVoucherValidator{
    @Override
    public boolean isValid(List<Voucher> vouchers, OrderRequest request) {
        vouchers.forEach(voucher -> {
            checkValidDate(voucher);
            if (voucher.getQuantity() <= 0) {
                throw new BadRequestException("Voucher is out of stock");
            }
            if (request.getTotalProductPrice() < voucher.getMinimumToApply()) {
                throw new BadRequestException("Minimum order value is not met");
            }
        });
        return true;
    }

    private void checkValidDate(Voucher voucher) {
        if (voucher.getValidFrom().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot use this voucher now");
        }
        if (voucher.getValidTo().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Voucher is expired");
        }
    }
}
