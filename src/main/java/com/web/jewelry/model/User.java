package com.web.jewelry.model;

import com.web.jewelry.enums.EGender;
import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.enums.EUserStatus;
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
    @Enumerated(EnumType.STRING)
    private EGender gender;
    private String phone;
    private LocalDate dob;
    private String avatar;
    @Enumerated(EnumType.STRING)
    private EUserRole role;
    @Enumerated(EnumType.STRING)
    private EUserStatus status;
    private LocalDateTime joinAt;
}
