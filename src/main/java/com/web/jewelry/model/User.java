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
    protected Long id;
    protected String username;
    protected String password;
    protected String email;
    protected String fullName;
    @Enumerated(EnumType.STRING)
    protected EGender gender;
    protected String phone;
    protected LocalDate dob;
    protected String avatar;
    @Enumerated(EnumType.STRING)
    protected EUserRole role;
    @Enumerated(EnumType.STRING)
    protected EUserStatus status;
    protected LocalDateTime joinAt;
    protected String backupToken;
    protected LocalDateTime backupTokenExpireAt;
}
