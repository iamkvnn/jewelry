package com.web.jewelry.dto.response;

import com.web.jewelry.enums.EReturnReason;
import lombok.Data;

import java.util.List;

@Data
public class ReturnItemResponse {
    private Long id;
    private Long quantity;
    private EReturnReason reason;
    private String description;
    private List<ImageResponse> proofImages;
}
