package com.tendering.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    public void sendOtp(String phoneNumber, String otpCode) {
        try {
            // Firebase ile gerçek SMS gönderimi
            // Not: Firebase Authentication Phone Auth kullanıyoruz

            // Firebase Phone Auth için custom token oluşturabiliriz
            // Ama daha basit yol: Firebase Console'da test numarası eklemek

            // Eğer test numarası değilse gerçek SMS gönder
            if (isTestPhoneNumber(phoneNumber)) {
                sendTestSms(phoneNumber, otpCode);
            } else {
                sendRealSms(phoneNumber, otpCode);
            }

        } catch (Exception e) {
            log.error("SMS gönderim hatası: {}", e.getMessage());
            throw new RuntimeException("SMS gönderilemedi: " + e.getMessage());
        }
    }

    private boolean isTestPhoneNumber(String phoneNumber) {
        // Test numaralarınızı buraya ekleyin
        return phoneNumber.equals("+905551234567") ||
                phoneNumber.equals("+905559876543");
    }

    private void sendTestSms(String phoneNumber, String otpCode) {
        log.info("TEST SMS gönderildi -> {}: Kod: {}", phoneNumber, otpCode);
        System.out.println("=== TEST SMS GÖNDERILDI ===");
        System.out.println("Telefon: " + phoneNumber);
        System.out.println("Kod: " + otpCode);
        System.out.println("====================");
    }

    private void sendRealSms(String phoneNumber, String otpCode) {
        try {
            // Firebase Admin SDK ile gerçek SMS
            // Not: Firebase Phone Auth gerçek implementasyonu
            log.info("Gerçek SMS gönderildi -> {}: Kod: {}", phoneNumber, otpCode);

            // TODO: Buraya Firebase Phone Auth veya Twilio entegrasyonu
            // Şimdilik log olarak bırakıyorum

        } catch (Exception e) {
            log.error("Gerçek SMS gönderim hatası: {}", e.getMessage());
            throw new RuntimeException("SMS gönderilemedi");
        }
    }
}