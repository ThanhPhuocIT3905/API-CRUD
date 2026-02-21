package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtTokenProvider: Lớp tiện ích để tạo và xác thực JWT tokens
 * 
 * Chức năng chính:
 * - Tạo JWT token từ authentication
 * - Xác thực token hợp lệ
 * - Lấy thông tin user từ token
 * - Kiểm tra token hết hạn
 */
@Component
public class JwtTokenProvider {

    // Secret key để ký JWT token - nên được lưu trong environment variables
    @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    // Thời gian hết hạn của token (24 giờ)
    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    /**
     * Lấy secret key để ký token
     * Sử dụng HMAC-SHA512 algorithm
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Tạo JWT token từ authentication object
     * @param authentication Đối tượng authentication chứa thông tin user
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        // Lấy thông tin user từ authentication
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        
        // Tạo token với các claims
        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // Set username là subject
                .issuedAt(new Date()) // Thời gian tạo token
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)) // Thời gian hết hạn
                .signWith(getSigningKey()) // Ký token với secret key
                .compact();
    }

    /**
     * Lấy username từ JWT token
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }

    /**
     * Xác thực JWT token có hợp lệ không
     * @param token JWT token cần xác thực
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean validateToken(String token) {
        try {
            // Parse token với secret key
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature: " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        }
        return false;
    }
}
