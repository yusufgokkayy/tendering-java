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
public class CreditCardResponse {

    private String id; // publicId
    private String cardHolderName;
    private String maskedCardNumber;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String paymentGateway;
    private Boolean isVerified;
    private LocalDateTime verificationDate;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}