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
public class WithdrawRequest {

    @NotNull(message = "Tutar gereklidir")
    @DecimalMin(value = "0.01", message = "Tutar en az 0.01 olmalıdır")
    private BigDecimal amount;

    private String bankAccountId; // Kullanıcının çekim yapacağı banka hesabı ID'si

    private String description;
}