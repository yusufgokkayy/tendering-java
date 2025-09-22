package com.tendering.service;

import com.tendering.dto.common.ApiResponse;
import com.tendering.model.InvalidatedToken;
import com.tendering.repository.InvalidatedTokenRepository;
import com.tendering.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LogoutService {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public ApiResponse<String> logout(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ApiResponse.error("Invalid token format.");
        }
        String jwtToken = token.substring(7);

        try {
            // Bu metot isimlerinin JwtUtil'de olduğundan emin oluyoruz
            String username = jwtUtil.extractUsername(jwtToken);
            Date expirationDate = jwtUtil.extractExpiration(jwtToken);

            // Token'ın süresi dolmuşsa zaten geçersizdir, tekrar kaydetmeye gerek yok.
            if (expirationDate.before(new Date())) {
                return ApiResponse.success("Token already expired.", null);
            }

            InvalidatedToken invalidatedToken = new InvalidatedToken();
            invalidatedToken.setToken(jwtToken);
            invalidatedToken.setExpirationDate(expirationDate);

            invalidatedTokenRepository.save(invalidatedToken);

            return ApiResponse.success("Logout successful.", null);
        } catch (Exception e) {
            return ApiResponse.error("Logout failed: " + e.getMessage());
        }
    }
}