package com.web.jewelry.model;

import com.web.jewelry.enums.EReturnReason;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ReturnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;

    @Enumerated(EnumType.STRING)
    private EReturnReason reason;
    private String description;

    @OneToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @OneToMany(mappedBy = "returnItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProofImage> proofImages;
}
