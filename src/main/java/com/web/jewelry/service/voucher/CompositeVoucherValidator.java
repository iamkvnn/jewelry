package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.model.Voucher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CompositeVoucherValidator implements IVoucherValidator {
    private final List<IVoucherValidator> validators;

    public CompositeVoucherValidator(List<IVoucherValidator> validators) {
        this.validators = validators;
    }

    public void addValidator(IVoucherValidator validator) {
        validators.add(validator);
    }

    public void removeValidator(IVoucherValidator validator) {
        validators.remove(validator);
    }

    @Override
    public boolean isValid(Voucher voucher, OrderRequest request) {
        return validators.stream()
                .allMatch(validator -> validator.isValid(voucher, request));
    }

    @Override
    public String getErrorMessage(Voucher voucher, OrderRequest request) {
        return validators.stream()
                .map(validator -> validator.getErrorMessage(voucher, request))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
