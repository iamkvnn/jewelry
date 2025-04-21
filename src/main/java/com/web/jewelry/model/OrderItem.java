package com.web.jewelry.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long quantity;
    private Long price;
    private Long discountPrice;
    private Long totalPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReturnItem returnItem;

    @ManyToOne  
    @JoinColumn(name = "product_size_id")
    private ProductSize productSize;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
