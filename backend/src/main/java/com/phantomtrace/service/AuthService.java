package com.phantomtrace.service;

import com.phantomtrace.auth.AuthResponse;
import com.phantomtrace.auth.LoginRequest;
import com.phantomtrace.auth.RegisterRequest;
import com.phantomtrace.auth.UserDto;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.UserRepository;
import com.phantomtrace.security.JwtService;
import com.phantomtrace.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication business logic: registration, login, current-user retrieval.
 * Passwords are stored only as BCrypt hashes; login failures return a uniform
 * message to prevent user enumeration.
 */
@Service
public class AuthService {

    private static final String LOGIN_FAILURE_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(
                request.email(),
                passwordHash,
                request.fullName(),
                UserRole.ROLE_ANALYST
        );
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Fetch by email; failure is intentionally indistinguishable from a wrong password
        // to avoid leaking which emails are registered.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(LOGIN_FAILURE_MESSAGE, HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(LOGIN_FAILURE_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        UUID userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Authenticated user not found", HttpStatus.NOT_FOUND));
        return UserDto.from(user);
    }
}
