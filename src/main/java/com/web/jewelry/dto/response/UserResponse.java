package com.web.jewelry.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.web.jewelry.enums.EGender;
import com.web.jewelry.enums.EMembershiprank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private LocalDate dob;
    private EGender gender;
    private String status;
    private EMembershiprank membershipRank;
    private Long totalSpent;
    private Boolean isSubscribedForNews;
    private String role;
}
