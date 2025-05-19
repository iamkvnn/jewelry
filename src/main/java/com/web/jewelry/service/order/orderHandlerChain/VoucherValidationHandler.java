package com.web.jewelry.service.order.orderHandlerChain;

import com.web.jewelry.model.Order;
import com.web.jewelry.model.Voucher;
import com.web.jewelry.service.voucher.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class VoucherValidationHandler extends OrderHandler {
    private final IVoucherService voucherService;

    @Override
    public Order process(OrderHandlerContext context) {
        List<Voucher> vouchers = voucherService.validateVouchers(context.getOrderRequest());
        context.setVouchers(vouchers);
        return processNext(context);
    }
}
