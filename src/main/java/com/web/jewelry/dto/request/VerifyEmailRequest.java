package com.web.jewelry.dto.request;

import lombok.Data;

@Data
public class VerifyEmailRequest {
    String email;
    String code;
}
