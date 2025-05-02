package com.web.jewelry.model;

import com.web.jewelry.enums.EOrderStatus;
import com.web.jewelry.enums.EPaymentMethod;
import com.web.jewelry.enums.EShippingMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "orders")
public class Order {
    @Id
    private String id;
    private Long totalProductPrice;
    private Long totalPrice;
    private Long shippingFee;
    private Long freeShipDiscount;
    private Long promotionDiscount;
    @Enumerated(EnumType.STRING)
    private EShippingMethod shippingMethod;
    @Enumerated(EnumType.STRING)
    private EOrderStatus status;
    @Enumerated(EnumType.STRING)
    private EPaymentMethod paymentMethod;
    private String note;
    private LocalDateTime orderDate;
    private boolean isReviewed;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private CODPayment codPayment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private MomoPayment momoPayment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private VNPayPayment vnPayPayment;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderVoucher> vouchers;
}
