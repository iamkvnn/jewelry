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
public class VNPayPayment extends Payment{
    private String transactionNumber;
    private String bank;
    private String vnPayResponseCode;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
