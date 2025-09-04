package com.tendering.security.jwt;

import com.tendering.model.User;
import com.tendering.repository.UserRepository;
import com.tendering.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        String username = null;
        String jwtToken = null;

        logger.debug("Gelen istek yolu: {}", requestURI);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);

            // Token'ın kara listede olup olmadığını kontrol et
            if (jwtToken != null && jwtUtil.isTokenInvalidated(jwtToken)) {
                logger.warn("Geçersiz kılınmış token erişim girişimi: {}, URI: {}", maskToken(jwtToken), requestURI);
                chain.doFilter(request, response);
                return;
            }

            try {
                if (jwtToken != null) {
                    username = jwtUtil.extractUsername(jwtToken);
                    logger.debug("Token doğrulama başarılı: kullanıcı={}, URI: {}", username, requestURI);
                }
            } catch (ExpiredJwtException e) {
                logger.warn("Süresi dolmuş JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            catch (Exception e) {
                // Token çözümlenirken hata oluştu
                logger.error("JWT token validation error: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtToken != null && jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("User authenticated: {}", username);
                }
            } catch (Exception e) {
                logger.error("Authentication process error: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    // Token'ın bir kısmını maskelemek için yardımcı metot
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "[PROTECTED]";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}