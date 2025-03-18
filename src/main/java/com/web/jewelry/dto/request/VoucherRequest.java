package com.web.jewelry.dto.request;

import com.web.jewelry.enums.EVoucherType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoucherRequest {
    private String code;
    private String name;
    private Long discountRate;
    private Long minimumToApply;
    private Long applyLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Long quantity;
    private Long limitUsePerCustomer;
    private EVoucherType type;
    private List<VoucherApplicabilityRequest> applicabilities;
}
