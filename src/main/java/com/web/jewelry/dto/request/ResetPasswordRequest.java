package com.web.jewelry.dto.request;

import com.web.jewelry.enums.EUserRole;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private EUserRole role;
    private String newPassword;
    private String code;
    private String token;
}
