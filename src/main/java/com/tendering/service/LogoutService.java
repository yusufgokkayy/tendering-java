package com.tendering.service;

import com.tendering.dto.ApiResponse;
import com.tendering.model.InvalidatedToken;
import com.tendering.repository.InvalidatedTokenRepository;
import com.tendering.security.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class LogoutService {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public ApiResponse<String> logout(String token) {
        // Token'ın JWT formatını kontrol et
        // SONRA - Seçenek 1: Bearer (boşluk) formatına geçiş
        if (token == null || !token.startsWith("Bearer ")) {
            return ApiResponse.error("Invalid token format.");
        }
        String jwtToken = token.substring(7);

        try {
            // Token'dan kullanıcı bilgilerini al
            String username = jwtUtil.extractUsername(jwtToken);

            // Token'ın son kullanma tarihini al
            Date expirationDate = jwtUtil.extractExpiration(jwtToken);

            // Token'ı kara listeye ekle
            InvalidatedToken invalidatedToken = new InvalidatedToken();
            invalidatedToken.setToken(jwtToken);
            invalidatedToken.setUsername(username);
            invalidatedToken.setExpirationDate(expirationDate);
            invalidatedToken.setInvalidatedAt(new Date());

            invalidatedTokenRepository.save(invalidatedToken);

            return ApiResponse.success("Logout successful.", null);
        }
         catch (Exception e) {
            return ApiResponse.error("Logout failed: " + e.getMessage());
         }
    }
}
