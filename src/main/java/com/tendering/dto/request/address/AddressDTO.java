package com.tendering.dto.request.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private String id; // publicId olarak kullanılacak

    @NotBlank(message = "Adres başlığı gereklidir")
    @Size(max = 50, message = "Adres başlığı en fazla 50 karakter olabilir")
    private String addressTitle;

    @NotBlank(message = "Ad soyad gereklidir")
    private String fullName;

    private String phoneNumber;

    @NotBlank(message = "Ülke gereklidir")
    private String country;

    @NotBlank(message = "Şehir gereklidir")
    private String city;

    @NotBlank(message = "İlçe gereklidir")
    private String district;

    private String zipCode;

    @NotBlank(message = "Adres satırı gereklidir")
    private String addressLine1;

    private String addressLine2;

    private Boolean isDefault;

    private Boolean isInvoiceAddress;

    // Fatura adresi bilgileri
    private String taxNumber;
    private String taxOffice;
    private String companyName;

    private String userId; // User publicId
}