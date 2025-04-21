package com.web.jewelry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ProductSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String size;
    private Long stock;
    private Long price;
    private Long sold;
    private Long discountPrice;
    private Long discountRate;
    private boolean isDeleted;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
