package com.web.jewelry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.web.jewelry.dto.response.ReviewResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Staff extends User{
    @OneToOne(mappedBy = "staff")
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserNotification> notifications;

    @JsonIgnore
    @OneToMany(mappedBy = "responseBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewResponse> reviewResponses;
}
