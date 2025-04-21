package com.web.jewelry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VNPayIPNResponse {
    private String RspCode;
    private String Message;
}
