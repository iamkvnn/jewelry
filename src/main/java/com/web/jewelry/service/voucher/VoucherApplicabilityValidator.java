package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.model.Voucher;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class VoucherApplicabilityValidator implements IVoucherValidator{
    protected final IUserService userService;

    @Override
    public boolean isValid(List<Voucher> vouchers, OrderRequest request) {
        vouchers.forEach(voucher -> voucher.getVoucherApplicabilities().stream()
                .filter(voucherApplicability -> switch (voucherApplicability.getType()) {
                    case ALL -> true;
                    case PRODUCT -> request.getCartItems().stream().map(
                            cartItem -> cartItem.getProduct().getId()
                    ).toList().contains(voucherApplicability.getApplicableObjectId());
                    case CATEGORY -> request.getCartItems().stream().map(
                            cartItem -> cartItem.getProduct().getCategory() != null ? cartItem.getProduct().getCategory().getId() : Long.valueOf(-1)
                    ).toList().contains(voucherApplicability.getApplicableObjectId());
                    case COLLECTION -> request.getCartItems().stream().map(
                            cartItem -> cartItem.getProduct().getCollection() != null ? cartItem.getProduct().getCollection().getId() : Long.valueOf(-1)
                    ).toList().contains(voucherApplicability.getApplicableObjectId());
                    case CUSTOMER -> userService.getCurrentUser().getId().equals(voucherApplicability.getApplicableObjectId());
                }).findFirst().orElseThrow(() -> new BadRequestException("Invalid voucher applicability")));
        return true;
    }
}
