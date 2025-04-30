package com.web.jewelry.dto.request;

import com.web.jewelry.enums.EReturnReason;
import lombok.Data;

@Data
public class ReturnItemRequest {
        private Long itemId;
        private Long quantity;
        private EReturnReason returnReason;
        private String description;
}
