package com.web.jewelry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.web.jewelry.enums.EProductStatus;
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
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(length = 16777216)
    private String description;
    private String material;
    @Enumerated(EnumType.STRING)
    private EProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    @OneToMany(mappedBy = "product")
    private List<ProductSize> productSizes;

    @OneToMany(mappedBy = "product")
    private List<AttributeValue> attributes;

    @OneToMany(mappedBy = "product")
    private List<ProductImage> images;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<WishlistItem> wishlistItems;
}
