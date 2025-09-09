package com.tendering.dto.request.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountCreateRequest {

    @NotBlank(message = "Hesap sahibi adı gereklidir")
    @Size(min = 2, max = 100, message = "Hesap sahibi adı 2-100 karakter arasında olmalıdır")
    private String accountHolderName;

    @NotBlank(message = "IBAN gereklidir")
    @Pattern(regexp = "^TR\\d{2}\\d{4}\\d{1}\\d{16}$", message = "Geçerli bir TR IBAN giriniz")
    private String iban;

    @NotBlank(message = "Banka adı gereklidir")
    @Size(min = 2, max = 100, message = "Banka adı 2-100 karakter arasında olmalıdır")
    private String bankName;

    @Size(max = 20, message = "Şube kodu en fazla 20 karakter olabilir")
    private String branchCode;

    @Builder.Default
    private Boolean isDefault = false;
}