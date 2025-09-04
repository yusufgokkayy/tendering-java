package com.tendering.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterByPhoneRequest {

    @NotBlank(message = "Doğrulama anahtarı (idToken) boş olamaz")
    private String idToken;

    @NotBlank(message = "Ad boş olamaz")
    @Size(min = 2, message = "Ad en az 2 karakter olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(min = 2, message = "Soyad en az 2 karakter olmalıdır")
    private String lastName;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;

    // EKLEME: Telefon numarası alanı ekleniyor
    @NotBlank(message = "Telefon numarası boş olamaz")
    private String phoneNumber;

    // UI'da doğum tarihi de vardı, isteğe bağlı olarak eklenebilir.
    // private String birthDate;
}