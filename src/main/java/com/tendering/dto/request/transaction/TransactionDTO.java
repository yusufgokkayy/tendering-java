package com.tendering.dto.request.transaction;

import com.tendering.model.Transaction;
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
public class TransactionDTO {

    private String id; // publicId olarak kullanılacak
    private String walletId; // Wallet publicId
    private String type;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal currentBalance;
    private String description;
    private String referenceId;
    private String escrowId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}