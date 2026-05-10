
package org.example.aquabackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        // Ensure key is at least 256 bits for HS256
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token for the given username
     */
    public String generateToken(String username) {
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
        logger.info("Generated token for user: {}", username);
        return token;
    }

    /**
     * Validate a token against a username
     */
    public boolean validateToken(String token, String username) {
        try {
        String tokenUsername = extractUsername(token);
        boolean isValid = (username.equals(tokenUsername) && !isTokenExpired(token));
        logger.info("Token validation result for user {}: {}", username, isValid);
        return isValid;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
    }
    }

    /**
     * Extract username from a JWT token
     */
    public String extractUsername(String token) {
        try {
        String username = extractAllClaims(token).getSubject();
        logger.info("Extracted username from token: {}", username);
        return username;
        } catch (Exception e) {
            logger.error("Failed to extract username from token: {}", e.getMessage());
            return null;
    }
    }

    /**
     * Check if the token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
        boolean expired = extractAllClaims(token).getExpiration().before(new Date());
        logger.info("Token expired: {}", expired);
        return expired;
        } catch (Exception e) {
            logger.error("Failed to check token expiration: {}", e.getMessage());
            return true;
    }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}