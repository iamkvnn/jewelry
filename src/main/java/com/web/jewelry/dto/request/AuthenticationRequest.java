package com.web.jewelry.dto.request;

import com.web.jewelry.enums.EUserRole;
import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
    private boolean isLoginWithGoogle = false;
    private EUserRole role;
}
