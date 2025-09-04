package com.tendering.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OtpService {

    private static final String OTP_PREFIX = "otp:";
    private static final String PHONE_SESSION_PREFIX = "phone_session:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int SESSION_EXPIRY_MINUTES = 30;
    private static final int RATE_LIMIT_MINUTES = 1;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SmsService smsService;

    public void sendOtp(String phoneNumber) {
        // Rate limiting kontrolü
        if (isRateLimited(phoneNumber)) {
            throw new RuntimeException("OTP çok sık talep edildi. Lütfen 1 dakika bekleyin.");
        }

        String otpCode = generateOtpCode();
        String otpKey = OTP_PREFIX + phoneNumber;
        String rateLimitKey = RATE_LIMIT_PREFIX + phoneNumber;

        // OTP'yi Redis'e kaydet
        redisTemplate.opsForValue().set(otpKey, otpCode, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        // Rate limiting için kayıt
        redisTemplate.opsForValue().set(rateLimitKey, LocalDateTime.now().toString(), RATE_LIMIT_MINUTES, TimeUnit.MINUTES);

        // SMS gönder
        smsService.sendOtp(phoneNumber, otpCode);

        log.info("OTP gönderildi: {} - Kod: {}", phoneNumber, otpCode);

        // RedisInsight'ta görmek için ek bilgi
        String debugKey = "debug:otp:" + phoneNumber;
        String debugInfo = String.format("Phone: %s, OTP: %s, Time: %s",
                phoneNumber, otpCode, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        redisTemplate.opsForValue().set(debugKey, debugInfo, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
    }

    public boolean verifyOtp(String phoneNumber, String otpCode) {
        String otpKey = OTP_PREFIX + phoneNumber;
        String storedOtp = (String) redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            log.warn("OTP bulunamadı veya süresi dolmuş: {}", phoneNumber);
            return false;
        }

        boolean isValid = storedOtp.equals(otpCode);

        if (isValid) {
            // OTP doğru ise ilgili key'leri sil ve session oluştur
            redisTemplate.delete(otpKey);
            redisTemplate.delete("debug:otp:" + phoneNumber);
            createPhoneVerificationSession(phoneNumber);
            log.info("OTP başarıyla doğrulandı: {}", phoneNumber);
        } else {
            log.warn("Geçersiz OTP denemesi: {} - Beklenen: {}, Gelen: {}", phoneNumber, storedOtp, otpCode);
        }

        return isValid;
    }

    public boolean isPhoneVerified(String phoneNumber) {
        String sessionKey = PHONE_SESSION_PREFIX + phoneNumber;
        Boolean exists = redisTemplate.hasKey(sessionKey);
        log.debug("Telefon doğrulama durumu kontrol edildi: {} - Durum: {}", phoneNumber, exists);
        return Boolean.TRUE.equals(exists);
    }

    public void clearPhoneVerificationSession(String phoneNumber) {
        String sessionKey = PHONE_SESSION_PREFIX + phoneNumber;
        redisTemplate.delete(sessionKey);
        log.info("Telefon doğrulama session'ı temizlendi: {}", phoneNumber);
    }

    private void createPhoneVerificationSession(String phoneNumber) {
        String sessionKey = PHONE_SESSION_PREFIX + phoneNumber;
        String sessionData = String.format("verified_at_%s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        redisTemplate.opsForValue().set(sessionKey, sessionData, SESSION_EXPIRY_MINUTES, TimeUnit.MINUTES);
        log.info("Telefon doğrulama session'ı oluşturuldu: {}", phoneNumber);
    }

    private boolean isRateLimited(String phoneNumber) {
        String rateLimitKey = RATE_LIMIT_PREFIX + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey));
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    // Debug için Redis'teki tüm OTP verilerini listele
    public void debugListAllOtpData() {
        log.info("=== REDIS DEBUG BİLGİLERİ ===");

        // OTP key'lerini listele
        var otpKeys = redisTemplate.keys(OTP_PREFIX + "*");
        if (otpKeys != null) {
            for (Object key : otpKeys) {
                Object value = redisTemplate.opsForValue().get(key.toString());
                Long ttl = redisTemplate.getExpire(key.toString());
                log.info("OTP - Key: {}, Value: {}, TTL: {} saniye", key, value, ttl);
            }
        }

        // Session key'lerini listele
        var sessionKeys = redisTemplate.keys(PHONE_SESSION_PREFIX + "*");
        if (sessionKeys != null) {
            for (Object key : sessionKeys) {
                Object value = redisTemplate.opsForValue().get(key.toString());
                Long ttl = redisTemplate.getExpire(key.toString());
                log.info("SESSION - Key: {}, Value: {}, TTL: {} saniye", key, value, ttl);
            }
        }

        log.info("=== REDIS DEBUG BİTTİ ===");
    }
}