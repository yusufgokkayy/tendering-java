package com.tendering.dto.request.auth.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneVerificationRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "^\\+90[0-9]{10}$", message = "Geçerli bir Türkiye telefon numarası giriniz (+905xxxxxxxxx)")
    private String phoneNumber;
}