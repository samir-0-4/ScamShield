package com.phantomtrace.service;

import com.phantomtrace.auth.AuthResponse;
import com.phantomtrace.auth.LoginRequest;
import com.phantomtrace.auth.RegisterRequest;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.UserRepository;
import com.phantomtrace.security.JwtService;
import com.phantomtrace.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_persistsUserWithHashedPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("analyst@phantomtrace.local", "Test Analyst", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");
        when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo(request.email());
        assertThat(response.user().role()).isEqualTo(UserRole.ROLE_ANALYST);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmailThrowsConflict() {
        RegisterRequest request = new RegisterRequest("dup@example.com", "Dup", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void login_validCredentialsReturnsToken() {
        User user = new User("analyst@phantomtrace.local", "hashed-pw", "Test Analyst", UserRole.ROLE_ANALYST);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest(user.getEmail(), "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_wrongPasswordThrowsUnauthorizedWithGenericMessage() {
        User user = new User("analyst@phantomtrace.local", "hashed-pw", "Test Analyst", UserRole.ROLE_ANALYST);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "wrong")))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getMessage()).isEqualTo("Invalid email or password");
                });
    }

    @Test
    void login_unknownEmailThrowsUnauthorizedWithSameGenericMessage() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "password123")))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getMessage()).isEqualTo("Invalid email or password");
                });
    }

    @Test
    void getCurrentUser_returnsProfileForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        User user = new User("analyst@phantomtrace.local", "hashed-pw", "Test Analyst", UserRole.ROLE_ANALYST);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var dto = authService.getCurrentUser();

        assertThat(dto.email()).isEqualTo("analyst@phantomtrace.local");
        assertThat(dto.role()).isEqualTo(UserRole.ROLE_ANALYST);
    }

    @Test
    void getCurrentUser_throwsWhenAuthenticatedUserMissing() {
        UUID userId = UUID.randomUUID();
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
