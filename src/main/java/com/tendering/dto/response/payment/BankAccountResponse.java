package com.tendering.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {

    private String id; // publicId
    private String accountHolderName;
    private String iban;
    private String bankName;
    private String branchCode;
    private Boolean isVerified;
    private LocalDateTime verificationDate;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}