package com.tendering.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand; // <-- Bunun tanımlı olması lazım

    @Column(nullable = false, updatable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false)
    private String name;

    @Column(length = 5000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private BigDecimal startingPrice;

    private BigDecimal reservePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // Add the missing relationship to Auction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    // ProductImage sınıfı ile ilişki
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = ProductStatus.DRAFT;
        }
    }

    // Ürün durumu için enum
    public enum ProductStatus {
        DRAFT,      // Taslak
        PENDING,    // Onay bekliyor
        APPROVED,   // Onaylandı
        ACTIVE,     // Aktif (ihalede)
        SOLD,       // Satıldı
        UNSOLD,     // Satılmadı
        REMOVED     // Kaldırıldı
    }

    // Resim ekleme yardımcı metodu
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    // Resim silme yardımcı metodu
    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }
}