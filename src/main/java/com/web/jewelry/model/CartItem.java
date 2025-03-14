package com.web.jewelry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;
    private boolean isChecked;
    private boolean isInCheckout;
    private LocalDateTime addedAt;

    @ManyToOne
    @JoinColumn(name = "product_size_id")
    private ProductSize productSize;

    @OneToOne(mappedBy = "cartItem")
    private OrderItem orderItem;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
