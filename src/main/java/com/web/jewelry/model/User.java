package com.web.jewelry.model;

import com.web.jewelry.enums.EGender;
import com.web.jewelry.enums.EUserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private EGender gender;
    private String phone;
    private LocalDate dob;
    private EUserRole role;
    private LocalDateTime joinAt;
    private Boolean isVerified;
}
