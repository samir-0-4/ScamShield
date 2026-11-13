package com.phantomtrace.auth;

/**
 * Authentication response containing the issued JWT and the public user view.
 * Matches the shape the frontend {@code authService} already reads
 * ({@code response.data.token}, {@code response.data.user}).
 */
public record AuthResponse(
        String token,
        UserDto user
) {
}
