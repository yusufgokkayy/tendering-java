package com.tendering.dto.response.auction;

import com.tendering.model.Auction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponse {

    private UUID publicId;
    private String title;
    private String description;
    private UUID sellerPublicId;
    private String sellerName;
    private String type;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal reservePrice;
    private BigDecimal minBidIncrement;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String thumbnailUrl;
    private Boolean isFeatured;
    private Integer viewCount;
    private Boolean isLive;
    private String streamUrl;
    private Boolean chatEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int productsCount;
    private int bidsCount;

    public static AuctionResponse fromEntity(Auction auction) {
        return AuctionResponse.builder()
                .publicId(auction.getPublicId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .sellerPublicId(auction.getSeller().getPublicId())
                .sellerName(auction.getSeller().getFullName())
                .type(auction.getType().name())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .reservePrice(auction.getReservePrice())
                .minBidIncrement(auction.getMinBidIncrement())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .thumbnailUrl(auction.getThumbnailUrl())
                .isFeatured(auction.getIsFeatured())
                .viewCount(auction.getViewCount())
                .isLive(auction.getIsLive())
                .streamUrl(auction.getStreamUrl())
                .chatEnabled(auction.getChatEnabled())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .productsCount(auction.getProducts() != null ? auction.getProducts().size() : 0)
                .bidsCount(auction.getBids() != null ? auction.getBids().size() : 0)
                .build();
    }
}
