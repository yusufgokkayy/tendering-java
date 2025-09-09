package com.tendering.dto.request.wallet;

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
public class WalletDTO {

    private String id; // publicId olarak kullanılacak
    private String userId; // User publicId
    private BigDecimal balance;
    private BigDecimal pendingBalance;
    private BigDecimal holdBalance;
    private BigDecimal totalDeposited;
    private BigDecimal totalWithdrawn;
    private Boolean isLocked;
    private String lockReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}