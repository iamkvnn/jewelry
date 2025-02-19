package com.web.jewelry.dto.response;

import lombok.Data;

@Data
public class ApiResponse {
    private String code;
    private String message;
    private Object data;
    public ApiResponse(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
