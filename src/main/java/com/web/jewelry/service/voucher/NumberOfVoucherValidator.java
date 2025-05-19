package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.enums.EVoucherType;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.model.Voucher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NumberOfVoucherValidator implements IVoucherValidator {
    @Override
    public boolean isValid(List<Voucher> vouchers, OrderRequest request) {
        if (vouchers.size() > 2) {
            throw new BadRequestException("An order can have only two vouchers.");
        }
        int freeShipCount = 0;
        int promotionCount = 0;
        for (Voucher voucher : vouchers) {
            if (voucher.getType() == EVoucherType.FREESHIP) {
                freeShipCount++;
            } else if (voucher.getType() == EVoucherType.PROMOTION) {
                promotionCount++;
            }
        }

        if (freeShipCount > 1) {
            throw new BadRequestException("An order can have only one 'FREESHIP' voucher.");
        }
        if (promotionCount > 1) {
            throw new BadRequestException("An order can have only one 'PROMOTION' voucher.");
        }
        return true;
    }
}
