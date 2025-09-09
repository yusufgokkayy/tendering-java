package com.tendering.dto.request.auth.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerifyOtpForPasswordResetRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "\\+[0-9]{1,15}", message = "Telefon numarası +90XXXXXXXXXX formatında olmalıdır")
    private String phoneNumber;

    @NotBlank(message = "OTP kodu boş olamaz")
    @Size(min = 6, max = 6, message = "OTP kodu 6 haneli olmalıdır")
    private String otpCode;
}