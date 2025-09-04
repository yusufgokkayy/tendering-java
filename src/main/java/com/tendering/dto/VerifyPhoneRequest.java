package com.tendering.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerifyPhoneRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    private String phoneNumber;

    @NotBlank(message = "Doğrulama kodu boş olamaz")
    private String verificationCode;

    // Firebase ID token (isteğe bağlı)
    private String idToken;
}