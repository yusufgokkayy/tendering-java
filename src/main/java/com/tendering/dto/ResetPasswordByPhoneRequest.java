package com.tendering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordByPhoneRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "\\+[0-9]{1,15}", message = "Telefon numarası +90XXXXXXXXXX formatında olmalıdır")
    private String phoneNumber;

    @NotBlank(message = "OTP kodu boş olamaz")
    @Size(min = 6, max = 6, message = "OTP kodu 6 haneli olmalıdır")
    private String otpCode;

    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String newPassword;
}