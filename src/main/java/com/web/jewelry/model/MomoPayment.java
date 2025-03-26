package com.web.jewelry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class MomoPayment extends Payment {
    private String requestId;
    private Long transactionId;
    private int resultCode;
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
