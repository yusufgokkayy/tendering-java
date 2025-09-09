package com.tendering.controller;

import com.tendering.dto.request.auth.verification.SendOtpRequest;
import com.tendering.dto.request.auth.verification.VerifyOtpRequest;
import com.tendering.service.OtpService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@Slf4j
@CrossOrigin(origins = "*") // Frontend için
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            otpService.sendOtp(request.getPhoneNumber());

            response.put("success", true);
            response.put("message", "OTP başarıyla gönderildi");
            response.put("phoneNumber", request.getPhoneNumber());

            log.info("OTP gönderim başarılı: {}", request.getPhoneNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("OTP gönderim hatası: {}", e.getMessage());

            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("error", "OTP_SEND_FAILED");

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtpCode());

            if (isValid) {
                response.put("success", true);
                response.put("message", "OTP başarıyla doğrulandı");
                response.put("phoneNumber", request.getPhoneNumber());
                response.put("verified", true);

                log.info("OTP doğrulama başarılı: {}", request.getPhoneNumber());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Geçersiz veya süresi dolmuş OTP");
                response.put("error", "INVALID_OTP");

                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("OTP doğrulama hatası: {}", e.getMessage());

            response.put("success", false);
            response.put("message", "OTP doğrulanamadı: " + e.getMessage());
            response.put("error", "OTP_VERIFY_FAILED");

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/status/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(@PathVariable String phoneNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isVerified = otpService.isPhoneVerified(phoneNumber);

            response.put("success", true);
            response.put("phoneNumber", phoneNumber);
            response.put("isVerified", isVerified);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Status kontrol hatası: {}", e.getMessage());

            response.put("success", false);
            response.put("message", "Status kontrol edilemedi");
            response.put("error", "STATUS_CHECK_FAILED");

            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/session/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String phoneNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            otpService.clearPhoneVerificationSession(phoneNumber);

            response.put("success", true);
            response.put("message", "Session başarıyla temizlendi");
            response.put("phoneNumber", phoneNumber);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Session temizleme hatası: {}", e.getMessage());

            response.put("success", false);
            response.put("message", "Session temizlenemedi");
            response.put("error", "SESSION_CLEAR_FAILED");

            return ResponseEntity.badRequest().body(response);
        }
    }

    // Debug endpoint (sadece development için)
    @GetMapping("/debug/redis-data")
    public ResponseEntity<Map<String, Object>> debugRedisData() {
        Map<String, Object> response = new HashMap<>();

        try {
            otpService.debugListAllOtpData();

            response.put("success", true);
            response.put("message", "Redis debug bilgileri console'da yazdırıldı");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Debug bilgileri alınamadı");

            return ResponseEntity.badRequest().body(response);
        }
    }
}