package com.tendering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckPhoneRequest {

    @NotBlank(message = "Telefon numarası boş olamaz")
    // Örnek: +905551234567 formatını zorunlu kılmak için bir regex eklenebilir.
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Geçersiz telefon numarası formatı.")
    private String phoneNumber;
}