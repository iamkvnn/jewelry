package com.web.jewelry.dto.request;

import com.web.jewelry.enums.EGender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String fullName;
    private LocalDate dob;
    private EGender gender;
}
