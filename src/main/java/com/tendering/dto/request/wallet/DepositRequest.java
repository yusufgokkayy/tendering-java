package com.tendering.dto.request.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotNull(message = "Tutar gereklidir")
    @DecimalMin(value = "0.01", message = "Tutar en az 0.01 olmalıdır")
    private BigDecimal amount;

    private String paymentMethod; // "CREDIT_CARD", "BANK_TRANSFER" gibi

    private String referenceId; // Ödeme sağlayıcısı referans numarası

    private String description;
}