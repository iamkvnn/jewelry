package com.web.jewelry.dto.response;

import com.web.jewelry.enums.EGender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private LocalDate dob;
    private EGender gender;
    private String status;
    private String role;
}
