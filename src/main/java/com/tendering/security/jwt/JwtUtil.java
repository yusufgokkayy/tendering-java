package com.tendering.security.jwt;

import com.tendering.repository.InvalidatedTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tendering.model.User;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // 15 dakika (milisaniye cinsinden)
    private long expiration;

    @Value("${jwt.refreshExpiration}") // 30 gün (milisaniye cinsinden)
    private long refreshExpiration;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    // Access token oluşturma - Telefon numarası subject olarak kullan
    public String generateAccessToken(User user) {
        logger.debug("Access token üretiliyor: kullanıcı={}", user.getPhoneNumber());
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getPublicId().toString()); // Public ID kullan
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("name", user.getName());
        claims.put("surname", user.getSurname());
        claims.put("role", user.getRole());

        logger.debug("Access token üretildi: kullanıcı={}", user.getPhoneNumber());
        return createToken(claims, user.getPhoneNumber(), expiration); // Subject = phoneNumber
    }

    // Refresh token oluşturma
    public String generateRefreshToken(User user) {
        logger.debug("Refresh token üretiliyor: kullanıcı={}", user.getPhoneNumber());
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getPublicId().toString());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("name", user.getName());

        return createToken(claims, user.getPhoneNumber(), refreshExpiration);
    }

    // Token'dan User ID (UUID) çıkar
    public UUID extractUserId(String token) {
        String idStr = (String) extractAllClaims(token).get("id");
        return UUID.fromString(idStr);
    }

    // Token'dan telefon numarası çıkar
    public String extractPhoneNumber(String token) {
        return extractClaim(token, Claims::getSubject); // Subject artık phoneNumber
    }

    // Token oluşturma metodu (common)
    private String createToken(Map<String, Object> claims, String subject, long ttl) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttl);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Subject = phoneNumber
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token'dan username bilgisini al -> phoneNumber'a değiştir
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Artık phoneNumber döner
    }

    // Token'dan son geçerlilik tarihini al
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Token'dan istenilen bir bilgiyi al
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Token'dan tüm bilgileri al
    private Claims extractAllClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token);

        JwsHeader header = jws.getHeader();
        String alg = header.getAlgorithm();
        if (!"HS256".equals(alg)) {
            throw new JwtException("Unexpected JWT algorithm: " + alg);
        }

        return jws.getBody();
    }

    // Token'ın geçerlilik süresinin dolup dolmadığını kontrol et
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Token'ın geçerli olup olmadığını kontrol et
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String phoneNumber = extractPhoneNumber(token); // phoneNumber al
            boolean isValid = (phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired(token));
            logger.debug("Token doğrulama: telefon={}, sonuç={}", phoneNumber, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Token doğrulama hatası: {}", e.getMessage());
            return false;
        }
    }

    // Token'ın geçersiz kılınıp kılınmadığını kontrol et
    public boolean isTokenInvalidated(String token) {
        boolean invalidated = invalidatedTokenRepository.existsByToken(token);
        if (invalidated) {
            logger.warn("Geçersiz kılınmış token erişim girişimi");
        }
        return invalidated;
    }
}