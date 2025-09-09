package com.tendering.dto.request.auth.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Geçersiz telefon numarası formatı. Örnek: +905551234567"
    )
    private String phoneNumber;
}