package com.web.jewelry.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthenticationResponse {
    private UserResponse user;
    private String token;
}
