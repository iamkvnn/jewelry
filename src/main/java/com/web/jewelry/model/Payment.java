package com.web.jewelry.model;

import com.web.jewelry.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    protected Long amount;
    @Enumerated(EnumType.STRING)
    protected EPaymentStatus status;
    protected LocalDateTime paymentDate;
    protected String paymentInfo;
    protected String paymentMessage;
}
