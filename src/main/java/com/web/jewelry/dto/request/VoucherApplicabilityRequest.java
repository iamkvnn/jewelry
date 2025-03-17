package com.web.jewelry.dto.request;

import com.web.jewelry.enums.EVoucherApplicabilityType;
import lombok.Data;

@Data
public class VoucherApplicabilityRequest {
    private EVoucherApplicabilityType type;
    private Long applicableObjectId;
}
