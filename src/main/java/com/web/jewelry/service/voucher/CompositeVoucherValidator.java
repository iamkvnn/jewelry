package com.web.jewelry.service.voucher;

import com.web.jewelry.dto.request.OrderRequest;
import com.web.jewelry.model.Voucher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CompositeVoucherValidator implements IVoucherValidator {
    private List<IVoucherValidator> validators;
    private final NumberOfVoucherValidator numberOfVoucherValidator;
    private final VoucherBasicInformationValidator voucherBasicInformationValidator;
    private final VoucherApplicabilityValidator voucherApplicabilityValidator;
    private final VoucherLimitUseExceedValidator voucherLimitUseExceedValidator;

    @PostConstruct
    public void init() {
        validators = List.of(
                numberOfVoucherValidator,
                voucherBasicInformationValidator,
                voucherApplicabilityValidator,
                voucherLimitUseExceedValidator
        );
    }

    public void addValidator(IVoucherValidator validator) {
        validators.add(validator);
    }

    public void removeValidator(IVoucherValidator validator) {
        validators.remove(validator);
    }

    @Override
    public boolean isValid(List<Voucher> vouchers, OrderRequest request) {
        return validators.stream()
                .allMatch(validator -> validator.isValid(vouchers, request));
    }
}
