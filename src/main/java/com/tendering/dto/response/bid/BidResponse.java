package com.tendering.dto.response.bid;

import com.tendering.model.Bid;
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
public class BidResponse {

    private UUID publicId;
    private UUID auctionPublicId;
    private String auctionTitle;
    private UUID bidderPublicId;
    private String bidderName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public static BidResponse fromEntity(Bid bid) {
        return BidResponse.builder()
                .publicId(bid.getPublicId())
                .auctionPublicId(bid.getAuction().getPublicId())
                .auctionTitle(bid.getAuction().getTitle())
                .bidderPublicId(bid.getBidder().getPublicId())
                .bidderName(bid.getBidder().getFullName())
                .amount(bid.getAmount())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}
