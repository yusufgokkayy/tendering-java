package com.tendering.dto.request.auth.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginByPhoneRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    private String phoneNumber;

    @NotBlank(message = "Şifre boş olamaz")
    private String password;
}