package com.tendering.controller;

import com.tendering.dto.request.address.AddressDTO;
import com.tendering.dto.common.ApiResponse;
import com.tendering.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/user/{userPublicId}")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddressesOfUser(
            @PathVariable UUID userPublicId) {
        log.debug("Kullanıcı adresleri istendi: {}", userPublicId);
        List<AddressDTO> addresses = addressService.getUserAddresses(userPublicId);
        return ResponseEntity.ok(ApiResponse.success("Adresler başarıyla getirildi", addresses));
    }

    @GetMapping("/invoice/{userPublicId}")
    public ResponseEntity<ApiResponse<AddressDTO>> getInvoiceAddress(
            @PathVariable UUID userPublicId) {
        log.debug("Kullanıcı fatura adresi istendi: {}", userPublicId);
        AddressDTO invoiceAddress = addressService.getInvoiceAddress(userPublicId);
        return ResponseEntity.ok(ApiResponse.success("Fatura adresi başarıyla getirildi", invoiceAddress));
    }

    @PutMapping("/{addressPublicId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable UUID addressPublicId,
            @Valid @RequestBody AddressDTO addressDTO) {
        log.debug("Adres güncelleme isteği: {}", addressPublicId);
        AddressDTO updatedAddress = addressService.updateAddress(addressPublicId, addressDTO);
        return ResponseEntity.ok(ApiResponse.success("Adres başarıyla güncellendi", updatedAddress));
    }

    @PutMapping("/{addressPublicId}/set-invoice")
    public ResponseEntity<ApiResponse<AddressDTO>> setAsInvoiceAddress(
            @PathVariable UUID addressPublicId) {
        log.debug("Fatura adresi olarak ayarlama isteği: {}", addressPublicId);
        AddressDTO updatedAddress = addressService.setAsInvoiceAddress(addressPublicId);
        return ResponseEntity.ok(ApiResponse.success("Adres fatura adresi olarak ayarlandı", updatedAddress));
    }

    @DeleteMapping("/{addressPublicId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable UUID addressPublicId) {
        log.debug("Adres silme isteği: {}", addressPublicId);
        addressService.deleteAddress(addressPublicId);
        return ResponseEntity.ok(ApiResponse.success("Adres başarıyla silindi", null));
    }

    @PostMapping("/user/{userPublicId}")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @PathVariable UUID userPublicId,
            @Valid @RequestBody AddressDTO addressDTO) {
        log.debug("Yeni adres ekleme isteği: {}", userPublicId);
        AddressDTO savedAddress = addressService.addAddressForProduct(userPublicId, addressDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Adres başarıyla eklendi", savedAddress),
                HttpStatus.CREATED);
    }
}