package com.tendering.dto.request.auth.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    private String phoneNumber;

    @NotBlank(message = "OTP kodu boş olamaz")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP kodu 6 haneli olmalıdır")
    private String otpCode;
}