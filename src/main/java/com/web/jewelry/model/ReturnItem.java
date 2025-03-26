package com.web.jewelry.model;

import com.web.jewelry.enums.EReturnReason;
import jakarta.persistence.*;

import java.util.List;

//@Entity
public class ReturnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;

    @Enumerated(EnumType.STRING)
    private EReturnReason reason;
    private String description;

    private OrderItem orderItem;
//    private List<ProofImage> proofImages;
}
