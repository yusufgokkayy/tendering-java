package com.tendering.dto.request.payment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardCreateRequest {

    @NotBlank(message = "Kart sahibi adı gereklidir")
    @Size(min = 2, max = 100, message = "Kart sahibi adı 2-100 karakter arasında olmalıdır")
    private String cardHolderName;

    @NotBlank(message = "Kart numarası gereklidir")
    @Pattern(regexp = "^\\d{16}$", message = "Kart numarası 16 haneli olmalıdır")
    private String cardNumber;

    @NotNull(message = "Son kullanma ayı gereklidir")
    @Min(value = 1, message = "Ay 1-12 arasında olmalıdır")
    @Max(value = 12, message = "Ay 1-12 arasında olmalıdır")
    private Integer expiryMonth;

    @NotNull(message = "Son kullanma yılı gereklidir")
    @Min(value = 2024, message = "Geçerli bir yıl giriniz")
    private Integer expiryYear;

    @NotBlank(message = "CVV gereklidir")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV 3-4 haneli olmalıdır")
    private String cvv;

    @Size(max = 50, message = "Ödeme gateway adı en fazla 50 karakter olabilir")
    private String paymentGateway;

    @Builder.Default
    private Boolean isDefault = false;
}