package com.tendering.service;

import com.tendering.dto.common.ApiResponse;
import com.tendering.model.User;
import com.tendering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PhonePasswordResetService {

    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final int PASSWORD_RESET_SESSION_MINUTES = 15;

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Şifre sıfırlama için OTP gönderir
     */
    public ApiResponse<String> sendPasswordResetOtp(String phoneNumber) {
        // Kullanıcının var olup olmadığını kontrol et
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);

        if (userOpt.isEmpty()) {
            log.warn("Şifre sıfırlama isteği yapılan telefon numarası bulunamadı: {}", phoneNumber);
            // Güvenlik için gerçek hatayı gösterme
            return ApiResponse.success("Eğer telefon numarası sistemimizde kayıtlıysa, şifre sıfırlama kodu gönderilecektir.", null);
        }

        // OTP gönder - mevcut OtpService'i kullan
        try {
            otpService.sendOtp(phoneNumber);
            log.info("Şifre sıfırlama OTP kodu gönderildi: {}", phoneNumber);
            return ApiResponse.success("Şifre sıfırlama kodu telefonunuza gönderildi.", null);
        } catch (RuntimeException e) {
            log.error("Şifre sıfırlama OTP gönderimi başarısız: {}", e.getMessage());
            return ApiResponse.error("OTP gönderilirken bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Şifre sıfırlama için OTP doğrular
     */
    public ApiResponse<String> verifyPasswordResetOtp(String phoneNumber, String otpCode) {
        // Kullanıcı var mı kontrol et
        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            return ApiResponse.error("Kullanıcı bulunamadı");
        }

        // OTP doğrula
        if (!otpService.verifyOtp(phoneNumber, otpCode)) {
            return ApiResponse.error("Geçersiz veya süresi dolmuş doğrulama kodu");
        }

        // Şifre sıfırlama oturumu oluştur
        String passwordResetKey = PASSWORD_RESET_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(
                passwordResetKey,
                "verified_at_" + System.currentTimeMillis(),
                PASSWORD_RESET_SESSION_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("Şifre sıfırlama OTP doğrulandı, oturum başlatıldı: {}", phoneNumber);
        return ApiResponse.success("OTP doğrulandı. Şifrenizi sıfırlayabilirsiniz.", null);
    }

    /**
     * Şifre sıfırlama işlemini gerçekleştirir
     */
    @Transactional
    public ApiResponse<String> resetPassword(String phoneNumber, String otpCode, String newPassword) {
        // Şifre sıfırlama oturumu kontrol et
        String passwordResetKey = PASSWORD_RESET_PREFIX + phoneNumber;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(passwordResetKey))) {
            // OTP doğrulamasını tekrar kontrol et - ekstra güvenlik
            if (!otpService.verifyOtp(phoneNumber, otpCode)) {
                return ApiResponse.error("Geçersiz veya süresi dolmuş doğrulama kodu");
            }
        }

        // Kullanıcıyı bul
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Kullanıcı bulunamadı");
        }

        // Şifreyi güncelle
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Şifre sıfırlama oturumunu temizle
        redisTemplate.delete(passwordResetKey);

        log.info("Şifre başarıyla sıfırlandı: {}", phoneNumber);
        return ApiResponse.success("Şifreniz başarıyla sıfırlanmıştır. Yeni şifrenizle giriş yapabilirsiniz.", null);
    }
}