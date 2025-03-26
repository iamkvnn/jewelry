package com.web.jewelry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Staff extends User{
    @JsonIgnore
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserNotification> notifications;

    @JsonIgnore
    @OneToMany(mappedBy = "responseBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewReply> reviewResponses;
}
