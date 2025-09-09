package com.tendering.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.tendering.dto.request.auth.login.LoginByPhoneRequest;
import com.tendering.dto.request.auth.register.PreRegisterRequest;
import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.common.TokenResponse;
import com.tendering.dto.response.auth.TokenWithUserResponse;
import com.tendering.model.User;
import com.tendering.repository.UserRepository;
import com.tendering.security.jwt.JwtUtil;
import com.tendering.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    public boolean isPhoneNumberRegistered(String phoneNumber) {
        log.debug("Telefon numarası kontrol ediliyor: {}", phoneNumber);
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public ApiResponse<TokenWithUserResponse> registerWithVerifiedPhone(PreRegisterRequest request) {
        log.info("Doğrulanmış telefon ile yeni kullanıcı kaydı: {}", request.getPhoneNumber());

        // Telefon numarası kontrolü
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalStateException("Bu telefon numarası zaten sistemde kayıtlı.");
        }

        // Email kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Bu email adresi zaten sistemde kayıtlı.");
        }

        User newUser = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .name(request.getFirstName())
                .surname(request.getLastName())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .phoneNumberVerified(true)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Kullanıcı kaydı başarılı: {} - {}", savedUser.getPhoneNumber(), savedUser.getEmail());

        // Token oluştur
        String accessToken = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);
        refreshTokenService.saveRefreshToken(savedUser, refreshToken);

        // Hem token hem kullanıcı bilgilerini dön
        TokenWithUserResponse response = new TokenWithUserResponse(
                accessToken,
                refreshToken,
                userMapper.convertToDTO(savedUser)
        );

        return ApiResponse.success("Kullanıcı başarıyla kaydedildi ve oturum açıldı.", response);
    }

    // Telefon ve şifre ile giriş
    public ApiResponse<TokenResponse> loginByPhone(LoginByPhoneRequest request) {
        log.info("Telefon numarası ile giriş denemesi: {}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BadCredentialsException("Geçersiz kullanıcı bilgileri."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Geçersiz kullanıcı bilgileri.");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        refreshTokenService.saveRefreshToken(user, refreshToken);

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        log.info("Giriş başarılı: {}", user.getPhoneNumber());
        return ApiResponse.success("Login successful", tokenResponse);
    }

    // Telefon doğrulaması sonrası direkt giriş
    public ApiResponse<TokenResponse> verifyPhoneAndLogin(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BadCredentialsException("Kullanıcı bulunamadı."));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        refreshTokenService.saveRefreshToken(user, refreshToken);

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        return ApiResponse.success("Login successful", tokenResponse);
    }

    public ApiResponse<TokenResponse> refreshAccessToken(String refreshToken) {
        return refreshTokenService.refreshAccessToken(refreshToken);
    }

    private FirebaseToken verifyFirebaseToken(String idToken) throws FirebaseAuthException {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Firebase ID token boş olamaz.");
        }
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}