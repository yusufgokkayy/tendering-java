package com.tendering.service;

import com.tendering.dto.request.address.AddressDTO;
import com.tendering.dto.common.ApiResponse;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.Address;
import com.tendering.model.User;
import com.tendering.repository.AddressRepository;
import com.tendering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressDTO> getUserAddresses(UUID userPublicId) {
        log.debug("Kullanıcı adresleri getiriliyor: {}", userPublicId);
        List<Address> addresses = addressRepository.findByUserPublicId(userPublicId);
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AddressDTO getInvoiceAddress(UUID userPublicId) {
        log.debug("Kullanıcı fatura adresi getiriliyor: {}", userPublicId);
        User user = getUserByPublicId(userPublicId);

        Address invoiceAddress = addressRepository.findByUserAndIsInvoiceAddress(user, true)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura adresi bulunamadı"));

        return convertToDTO(invoiceAddress);
    }

    @Transactional
    public AddressDTO updateAddress(UUID addressPublicId, AddressDTO addressDTO) {
        log.debug("Adres güncelleniyor: {}", addressPublicId);

        Address address = addressRepository.findByPublicId(addressPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Adres bulunamadı: " + addressPublicId));

        updateAddressFromDTO(address, addressDTO);
        Address savedAddress = addressRepository.save(address);

        // Eğer varsayılan adres olarak işaretlendiyse, diğer adreslerin varsayılanını kaldır
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            updateDefaultAddress(savedAddress);
        }

        // Eğer fatura adresi olarak işaretlendiyse, diğer adreslerin fatura adresini kaldır
        if (Boolean.TRUE.equals(addressDTO.getIsInvoiceAddress())) {
            updateInvoiceAddress(savedAddress);
        }

        return convertToDTO(savedAddress);
    }

    @Transactional
    public AddressDTO setAsInvoiceAddress(UUID addressPublicId) {
        log.debug("Adres fatura adresi olarak ayarlanıyor: {}", addressPublicId);

        Address address = addressRepository.findByPublicId(addressPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Adres bulunamadı: " + addressPublicId));

        address.setIsInvoiceAddress(true);
        Address savedAddress = addressRepository.save(address);

        // Diğer adreslerin fatura adres işaretini kaldır
        updateInvoiceAddress(savedAddress);

        return convertToDTO(savedAddress);
    }

    @Transactional
    public void deleteAddress(UUID addressPublicId) {
        log.debug("Adres siliniyor: {}", addressPublicId);
        Address address = addressRepository.findByPublicId(addressPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Adres bulunamadı: " + addressPublicId));

        // Eğer varsayılan adres siliniyorsa, başka bir varsayılan adres belirlemeyi düşünebiliriz
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        boolean wasInvoice = Boolean.TRUE.equals(address.getIsInvoiceAddress());

        addressRepository.delete(address);

        // Silinen adres varsayılan ise ve başka adresler varsa, yeni bir varsayılan belirle
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUser(address.getUser());
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        // Silinen adres fatura adresi ise ve başka adresler varsa, yeni bir fatura adresi belirle
        if (wasInvoice) {
            List<Address> remainingAddresses = addressRepository.findByUser(address.getUser());
            if (!remainingAddresses.isEmpty()) {
                Address newInvoice = remainingAddresses.get(0);
                newInvoice.setIsInvoiceAddress(true);
                addressRepository.save(newInvoice);
            }
        }
    }

    @Transactional
    public AddressDTO addAddress(AddressDTO addressDTO) {
        log.debug("Yeni adres ekleniyor için kullanıcı: {}", addressDTO.getUserId());

        User user = getUserByPublicId(UUID.fromString(addressDTO.getUserId()));

        Address address = Address.builder()
                .user(user)
                .addressTitle(addressDTO.getAddressTitle())
                .fullName(addressDTO.getFullName())
                .phoneNumber(addressDTO.getPhoneNumber())
                .country(addressDTO.getCountry())
                .city(addressDTO.getCity())
                .district(addressDTO.getDistrict())
                .zipCode(addressDTO.getZipCode())
                .addressLine1(addressDTO.getAddressLine1())
                .addressLine2(addressDTO.getAddressLine2())
                .isDefault(addressDTO.getIsDefault())
                .isInvoiceAddress(addressDTO.getIsInvoiceAddress())
                .taxNumber(addressDTO.getTaxNumber())
                .taxOffice(addressDTO.getTaxOffice())
                .companyName(addressDTO.getCompanyName())
                .build();

        Address savedAddress = addressRepository.save(address);

        // Eğer bu ilk adres ise, varsayılan ve fatura adresi olarak belirle
        List<Address> userAddresses = addressRepository.findByUser(user);
        if (userAddresses.size() == 1) {
            savedAddress.setIsDefault(true);
            savedAddress.setIsInvoiceAddress(true);
            savedAddress = addressRepository.save(savedAddress);
        } else {
            // Eğer varsayılan adres olarak işaretlendiyse, diğer adreslerin varsayılanını kaldır
            if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
                updateDefaultAddress(savedAddress);
            }

            // Eğer fatura adresi olarak işaretlendiyse, diğer adreslerin fatura adresini kaldır
            if (Boolean.TRUE.equals(addressDTO.getIsInvoiceAddress())) {
                updateInvoiceAddress(savedAddress);
            }
        }

        return convertToDTO(savedAddress);
    }

    @Transactional
    public AddressDTO addAddressForProduct(UUID userPublicId, AddressDTO addressDTO) {
        addressDTO.setUserId(userPublicId.toString());
        return addAddress(addressDTO);
    }

    private void updateDefaultAddress(Address newDefaultAddress) {
        addressRepository.findByUser(newDefaultAddress.getUser()).stream()
                .filter(a -> !a.getId().equals(newDefaultAddress.getId()) && Boolean.TRUE.equals(a.getIsDefault()))
                .forEach(a -> {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                });
    }

    private void updateInvoiceAddress(Address newInvoiceAddress) {
        addressRepository.findByUser(newInvoiceAddress.getUser()).stream()
                .filter(a -> !a.getId().equals(newInvoiceAddress.getId()) && Boolean.TRUE.equals(a.getIsInvoiceAddress()))
                .forEach(a -> {
                    a.setIsInvoiceAddress(false);
                    addressRepository.save(a);
                });
    }

    private User getUserByPublicId(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + publicId));
    }

    private AddressDTO convertToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getPublicId().toString())
                .addressTitle(address.getAddressTitle())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .country(address.getCountry())
                .city(address.getCity())
                .district(address.getDistrict())
                .zipCode(address.getZipCode())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .isDefault(address.getIsDefault())
                .isInvoiceAddress(address.getIsInvoiceAddress())
                .taxNumber(address.getTaxNumber())
                .taxOffice(address.getTaxOffice())
                .companyName(address.getCompanyName())
                .userId(address.getUser().getPublicId().toString())
                .build();
    }

    private void updateAddressFromDTO(Address address, AddressDTO dto) {
        address.setAddressTitle(dto.getAddressTitle());
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setCountry(dto.getCountry());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setZipCode(dto.getZipCode());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setIsDefault(dto.getIsDefault());
        address.setIsInvoiceAddress(dto.getIsInvoiceAddress());
        address.setTaxNumber(dto.getTaxNumber());
        address.setTaxOffice(dto.getTaxOffice());
        address.setCompanyName(dto.getCompanyName());
    }
}