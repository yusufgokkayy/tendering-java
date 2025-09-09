package com.tendering.service;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.common.TokenResponse;
import com.tendering.exceptionHandlers.InvalidTokenException;
import com.tendering.model.RefreshToken;
import com.tendering.model.User;
import com.tendering.repository.RefreshTokenRepository;
import com.tendering.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30)); // 30 gün geçerlilik
        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> getRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isTokenInvalidated(String token) {
        return refreshTokenRepository.findByToken(token).isEmpty();
    }

    // EKLEME: refreshAccessToken metodu
    public ApiResponse<TokenResponse> refreshAccessToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

        if (tokenOpt.isEmpty()) {
            throw new InvalidTokenException("Geçersiz refresh token");
        }

        RefreshToken token = tokenOpt.get();

        // Token süresinin dolup dolmadığını kontrol et
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Süresi dolmuş token'ı sil
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException("Refresh token süresi dolmuş");
        }

        // Yeni access token oluştur
        User user = token.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        // Eski refresh token'ı sil ve yenisini kaydet
        refreshTokenRepository.delete(token);
        saveRefreshToken(user, newRefreshToken);

        TokenResponse tokenResponse = new TokenResponse(newAccessToken, newRefreshToken);
        return ApiResponse.success("Token yenilendi", tokenResponse);
    }
}