package com.tendering.service;

import com.tendering.dto.request.payment.CreditCardCreateRequest;
import com.tendering.dto.response.payment.CreditCardResponse;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.CreditCard;
import com.tendering.model.User;
import com.tendering.repository.CreditCardRepository;
import com.tendering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreditCardResponse createCreditCard(UUID userPublicId, CreditCardCreateRequest request) {
        log.debug("Kredi kartı oluşturuluyor - Kullanıcı: {}", userPublicId);

        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        // Kartı maskele
        String maskedCardNumber = maskCardNumber(request.getCardNumber());

        // Kart zaten kayıtlı mı kontrol et
        if (creditCardRepository.existsByUserAndMaskedCardNumber(user, maskedCardNumber)) {
            throw new IllegalStateException("Bu kart zaten kayıtlı");
        }

        // Son kullanma tarihi geçerli mi kontrol et
        validateExpiryDate(request.getExpiryMonth(), request.getExpiryYear());

        // Eğer default olarak işaretlenmişse, diğerlerini default olmaktan çıkar
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultCreditCard(user);
        }

        // Kart markasını belirle
        String cardBrand = determineCardBrand(request.getCardNumber());

        CreditCard creditCard = CreditCard.builder()
                .user(user)
                .cardHolderName(request.getCardHolderName())
                .maskedCardNumber(maskedCardNumber)
                .cardBrand(cardBrand)
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .paymentGateway(request.getPaymentGateway())
                .isDefault(request.getIsDefault())
                .isVerified(false) // Başlangıçta doğrulanmamış
                .isActive(true)
                .build();

        // Bu noktada gerçek uygulamada payment gateway ile kart tokenize edilir
        // creditCard.setGatewayToken(paymentGatewayService.tokenizeCard(request));

        CreditCard savedCard = creditCardRepository.save(creditCard);

        log.info("Kredi kartı oluşturuldu - ID: {}, Marka: {}", savedCard.getPublicId(), cardBrand);

        return convertToCreditCardResponse(savedCard);
    }

    public List<CreditCardResponse> getUserCreditCards(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        List<CreditCard> cards = creditCardRepository.findByUserAndIsActiveTrue(user);
        return cards.stream()
                .map(this::convertToCreditCardResponse)
                .collect(Collectors.toList());
    }

    public CreditCardResponse getCreditCardDetails(UUID userPublicId, UUID cardPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        CreditCard card = creditCardRepository.findByUserAndPublicId(user, cardPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kredi kartı bulunamadı: " + cardPublicId));

        return convertToCreditCardResponse(card);
    }

    @Transactional
    public CreditCardResponse setDefaultCreditCard(UUID userPublicId, UUID cardPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        CreditCard card = creditCardRepository.findByUserAndPublicId(user, cardPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kredi kartı bulunamadı: " + cardPublicId));

        // Diğer kartları default olmaktan çıkar
        resetDefaultCreditCard(user);

        // Bu kartı default yap
        card.setIsDefault(true);
        CreditCard updatedCard = creditCardRepository.save(card);

        log.info("Default kredi kartı değiştirildi - Kullanıcı: {}, Kart: {}", userPublicId, cardPublicId);

        return convertToCreditCardResponse(updatedCard);
    }

    @Transactional
    public void deleteCreditCard(UUID userPublicId, UUID cardPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        CreditCard card = creditCardRepository.findByUserAndPublicId(user, cardPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kredi kartı bulunamadı: " + cardPublicId));

        // Soft delete
        card.setIsActive(false);
        if (Boolean.TRUE.equals(card.getIsDefault())) {
            card.setIsDefault(false);
        }

        creditCardRepository.save(card);

        log.info("Kredi kartı silindi - Kullanıcı: {}, Kart: {}", userPublicId, cardPublicId);
    }

    @Transactional
    public CreditCardResponse verifyCreditCard(UUID cardPublicId, String gatewayToken) {
        CreditCard card = creditCardRepository.findByPublicId(cardPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kredi kartı bulunamadı: " + cardPublicId));

        card.setIsVerified(true);
        card.setVerificationDate(LocalDateTime.now());
        card.setGatewayToken(gatewayToken);
        CreditCard verifiedCard = creditCardRepository.save(card);

        log.info("Kredi kartı doğrulandı - ID: {}", cardPublicId);

        return convertToCreditCardResponse(verifiedCard);
    }

    private void resetDefaultCreditCard(User user) {
        creditCardRepository.findByUserAndIsDefaultTrueAndIsActiveTrue(user)
                .ifPresent(defaultCard -> {
                    defaultCard.setIsDefault(false);
                    creditCardRepository.save(defaultCard);
                });
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(12);
    }

    private String determineCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 1) {
            return "UNKNOWN";
        }

        String firstDigit = cardNumber.substring(0, 1);
        String firstTwoDigits = cardNumber.length() >= 2 ? cardNumber.substring(0, 2) : firstDigit;
        String firstFourDigits = cardNumber.length() >= 4 ? cardNumber.substring(0, 4) : firstTwoDigits;

        if (cardNumber.startsWith("4")) {
            return "VISA";
        } else if (firstTwoDigits.matches("^5[1-5].*") || firstFourDigits.matches("^222[1-9].*|^22[3-9]\\d.*|^2[3-6]\\d\\d.*|^27[0-1]\\d.*|^2720.*")) {
            return "MASTERCARD";
        } else if (firstTwoDigits.matches("^3[47].*")) {
            return "AMEX";
        } else if (firstFourDigits.matches("^6011.*") || firstTwoDigits.matches("^65.*")) {
            return "DISCOVER";
        } else {
            return "UNKNOWN";
        }
    }

    private void validateExpiryDate(Integer month, Integer year) {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            throw new IllegalArgumentException("Kart son kullanma tarihi geçmiş");
        }

        if (year > currentYear + 20) {
            throw new IllegalArgumentException("Geçersiz son kullanma tarihi");
        }
    }

    private CreditCardResponse convertToCreditCardResponse(CreditCard card) {
        return CreditCardResponse.builder()
                .id(card.getPublicId().toString())
                .cardHolderName(card.getCardHolderName())
                .maskedCardNumber(card.getMaskedCardNumber())
                .cardBrand(card.getCardBrand())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .paymentGateway(card.getPaymentGateway())
                .isVerified(card.getIsVerified())
                .verificationDate(card.getVerificationDate())
                .isDefault(card.getIsDefault())
                .isActive(card.getIsActive())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}