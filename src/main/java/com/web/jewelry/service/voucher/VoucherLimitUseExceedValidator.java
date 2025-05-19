package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.model.Voucher;
import com.web.jewelry.repository.VoucherRepository;
import com.web.jewelry.service.user.IUserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VoucherLimitUseExceedValidator extends VoucherApplicabilityValidator {
    private final VoucherRepository voucherRepository;
    public VoucherLimitUseExceedValidator(IUserService userService, VoucherRepository voucherRepository) {
        super(userService);
        this.voucherRepository = voucherRepository;
    }

    @Override
    public boolean isValid(List<Voucher> vouchers, OrderRequest request) {
        Long customerId = userService.getCurrentUser().getId();
        for (Voucher voucher : vouchers) {
            Long used = voucherRepository.countUsedByVoucherCodeAndCustomerId(voucher.getCode(), customerId);
            if (used >= voucher.getLimitUsePerCustomer()) {
                throw new BadRequestException("Voucher " + voucher.getCode() + " has been used up");
            }
        }
        return true;
    }
}
