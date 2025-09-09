package com.tendering.dto.request.auth.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForgotPasswordByPhoneRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "\\+[0-9]{1,15}", message = "Telefon numarası +90XXXXXXXXXX formatında olmalıdır")
    private String phoneNumber;
}