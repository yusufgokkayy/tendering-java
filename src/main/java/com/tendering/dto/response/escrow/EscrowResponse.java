package com.tendering.dto.response.escrow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowResponse {

    private String id; // publicId
    private String auctionId;
    private String buyerId;
    private String sellerId;
    private String winningBidId;
    private BigDecimal amount;
    private BigDecimal commissionAmount;
    private BigDecimal commissionRate;
    private String status;
    private String holdTransactionId;
    private String releaseTransactionId;
    private String refundTransactionId;
    private String notes;
    private LocalDateTime autoReleaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}