package com.web.jewelry.dto.response;

import com.web.jewelry.enums.EVoucherApplicabilityType;
import lombok.Data;

@Data
public class VoucherApplicabilityResponse {
    private Long id;
    private EVoucherApplicabilityType type;
    private Long applicableObjectId;
}
