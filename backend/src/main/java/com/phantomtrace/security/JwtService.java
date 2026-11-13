package com.phantomtrace.security;

import com.phantomtrace.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final int MIN_SECRET_LENGTH = 32;
    private static final Set<String> INSECURE_SECRETS = Set.of(
            "default_dev_secret_key_minimum_32_characters_for_hmac256_placeholder",
            "replace_with_a_secure_secret_minimum_32_characters_for_hmac256",
            "replace_with_a_secure_secret_minimum_32_characters_long_for_hmac256",
            "change_me",
            "your_jwt_secret_here");

    @Value("${phantomtrace.security.jwt.secret}")
    private String secret;

    @Value("${phantomtrace.security.jwt.expiration-ms:86400000}")
    private long expirationMillis;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank() || isInsecure(secret)
                || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "phantomtrace.security.jwt.secret must be a secure, non-placeholder value of at least "
                            + MIN_SECRET_LENGTH + " bytes for HS256. Configure JWT_SECRET.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isInsecure(String value) {
        return INSECURE_SECRETS.contains(value.trim())
                || value.chars().distinct().count() < 4;
    }

    public String generateToken(UUID userId, String email, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT rejected: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public UserRole extractRole(String token) {
        return UserRole.valueOf(parseClaims(token).get("role", String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
