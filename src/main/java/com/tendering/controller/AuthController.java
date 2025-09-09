package com.tendering.controller;

import com.tendering.dto.request.auth.login.LoginByPhoneRequest;
import com.tendering.dto.request.auth.register.PreRegisterRequest;
import com.tendering.dto.request.auth.verification.PhoneVerificationRequest;
import com.tendering.dto.request.auth.verification.VerifyOtpRequest;
import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.common.SimpleApiResponse;
import com.tendering.dto.common.TokenResponse;
import com.tendering.dto.request.auth.verification.VerifyPhoneRequest;
import com.tendering.dto.response.auth.TokenWithUserResponse;
import com.tendering.exceptionHandlers.InvalidTokenException;
import com.tendering.service.PhonePasswordResetService;
import com.tendering.dto.request.auth.password.ForgotPasswordByPhoneRequest;
import com.tendering.dto.request.auth.password.VerifyOtpForPasswordResetRequest;
import com.tendering.dto.request.auth.password.ResetPasswordByPhoneRequest;
import com.tendering.service.AuthService;
import com.tendering.service.OtpService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PhonePasswordResetService phonePasswordResetService;

    @Autowired
    private OtpService otpService;

    // ========== YENİ TELEFON DOĞRULAMA ENDPOİNTLERİ ==========

    @PostMapping("/send-otp")
    public ResponseEntity<SimpleApiResponse> sendOtp(@Valid @RequestBody PhoneVerificationRequest request) {
        try {
            // Telefon numarası zaten kayıtlı mı kontrol et
            if (authService.isPhoneNumberRegistered(request.getPhoneNumber())) {
                return ResponseEntity.badRequest()
                        .body(new SimpleApiResponse("Bu telefon numarası zaten sistemde kayıtlı."));
            }

            otpService.sendOtp(request.getPhoneNumber());
            return ResponseEntity.ok(new SimpleApiResponse("OTP kodu gönderildi."));

        } catch (RuntimeException e) {
            log.warn("OTP gönderimi başarısız: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new SimpleApiResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<SimpleApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtpCode());

            if (isValid) {
                return ResponseEntity.ok(new SimpleApiResponse("Telefon numarası doğrulandı."));
            } else {
                return ResponseEntity.badRequest()
                        .body(new SimpleApiResponse("Geçersiz veya süresi dolmuş OTP kodu."));
            }
        } catch (Exception e) {
            log.error("OTP doğrulama hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleApiResponse("OTP doğrulama sırasında hata oluştu."));
        }
    }

    @PostMapping("/pre-register")
    public ResponseEntity<ApiResponse<TokenWithUserResponse>> preRegister(@Valid @RequestBody PreRegisterRequest request) {
        try {
            // Telefon doğrulaması yapılmış mı kontrol et
            if (!otpService.isPhoneVerified(request.getPhoneNumber())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Telefon numarası henüz doğrulanmamış."));
            }

            // Kayıt işlemini gerçekleştir ve token al
            ApiResponse<TokenWithUserResponse> response = authService.registerWithVerifiedPhone(request);

            // Başarılı kayıt sonrası phone session'ı temizle
            otpService.clearPhoneVerificationSession(request.getPhoneNumber());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("Pre-register başarısız: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== TELEFON İLE GİRİŞ ==========

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> loginByPhone(@Valid @RequestBody LoginByPhoneRequest request) {
        try {
            ApiResponse<TokenResponse> response = authService.loginByPhone(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Giriş denemesi başarısız: {}", request.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyPhone(@Valid @RequestBody VerifyPhoneRequest request) {
        try {
            ApiResponse<TokenResponse> response = authService.verifyPhoneAndLogin(request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Phone verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SimpleApiResponse("Telefon doğrulaması başarısız: " + e.getMessage()));
        } catch (Exception e) {
            log.error("An error occurred during phone verification process.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleApiResponse("Telefon doğrulaması sırasında bir hata oluştu."));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            ApiResponse<TokenResponse> response = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // --- Password Reset Endpoints ---

    // AuthController sınıfına eklenecek endpoint'ler

    // Telefon ile şifre sıfırlama endpoint'leri
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPasswordByPhone(
            @Valid @RequestBody ForgotPasswordByPhoneRequest request) {
        ApiResponse<String> response = phonePasswordResetService.sendPasswordResetOtp(request.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-password-reset-otp")
    public ResponseEntity<ApiResponse<String>> verifyPasswordResetOtp(
            @Valid @RequestBody VerifyOtpForPasswordResetRequest request) {
        ApiResponse<String> response = phonePasswordResetService.verifyPasswordResetOtp(
                request.getPhoneNumber(), request.getOtpCode());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPasswordByPhone(
            @Valid @RequestBody ResetPasswordByPhoneRequest request) {
        ApiResponse<String> response = phonePasswordResetService.resetPassword(
                request.getPhoneNumber(), request.getOtpCode(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }
}