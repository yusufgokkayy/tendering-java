package com.tendering.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionType type;

    @Column(name = "start_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "current_price", precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "reserve_price", precision = 19, scale = 2)
    private BigDecimal reservePrice;

    @Column(name = "min_bid_increment", precision = 19, scale = 2)
    private BigDecimal minBidIncrement;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String status;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "is_live")
    private Boolean isLive;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(name = "chat_enabled")
    private Boolean chatEnabled;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bid> bids = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AuctionType {
        PRODUCT, LIST, LIVESTREAM
    }

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.minBidIncrement == null) {
            this.minBidIncrement = new BigDecimal("1.00");
        }
        if (this.currentPrice == null) {
            this.currentPrice = this.startPrice;
        }
        if (this.isFeatured == null) {
            this.isFeatured = false;
        }
        if (this.isLive == null) {
            this.isLive = false;
        }
        if (this.chatEnabled == null) {
            this.chatEnabled = true;
        }
    }
}