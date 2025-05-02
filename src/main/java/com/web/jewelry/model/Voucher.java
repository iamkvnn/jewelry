package com.web.jewelry.model;

import com.web.jewelry.enums.EVoucherType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private Long discountRate;
    private Long minimumToApply;
    private Long applyLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Long quantity;
    private Long limitUsePerCustomer;
    @Enumerated(EnumType.STRING)
    private EVoucherType type;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<VoucherApplicability> voucherApplicabilities;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<OrderVoucher> orders;
}
