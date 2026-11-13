package com.phantomtrace.security;

import com.phantomtrace.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-characters-long";
    private static final long EXPIRY_MS = 3_600_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMillis", EXPIRY_MS);
        jwtService.init();
    }

    @Test
    void generateToken_producesValidTokenWithCorrectClaims() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "analyst@phantomtrace.local", UserRole.ROLE_ANALYST);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo(UserRole.ROLE_ANALYST);
    }

    @Test
    void isTokenValid_returnsFalseForMalformedTokens() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForTokenSignedWithDifferentSecret() {
        JwtService other = new JwtService();
        ReflectionTestUtils.setField(other, "secret", "a-different-secret-that-is-also-32-chars!!");
        ReflectionTestUtils.setField(other, "expirationMillis", EXPIRY_MS);
        other.init();

        String foreignToken = other.generateToken(UUID.randomUUID(), "x@y.com", UserRole.ROLE_ADMIN);

        assertThat(jwtService.isTokenValid(foreignToken)).isFalse();
    }

    @Test
    void init_throwsWhenSecretIsTooShort() {
        JwtService bad = new JwtService();
        ReflectionTestUtils.setField(bad, "secret", "short");

        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void init_throwsWhenSecretIsBlank() {
        JwtService bad = new JwtService();
        ReflectionTestUtils.setField(bad, "secret", " ");

        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void init_throwsWhenSecretIsKnownPlaceholder() {
        JwtService bad = new JwtService();
        ReflectionTestUtils.setField(bad, "secret",
                "default_dev_secret_key_minimum_32_characters_for_hmac256_placeholder");

        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void init_throwsWhenSecretHasInsufficientEntropy() {
        JwtService bad = new JwtService();
        ReflectionTestUtils.setField(bad, "secret", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class);
    }
}
