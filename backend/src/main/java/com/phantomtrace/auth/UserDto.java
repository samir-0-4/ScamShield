package com.phantomtrace.auth;

import com.phantomtrace.entity.User;
import com.phantomtrace.enums.UserRole;

import java.util.UUID;

/**
 * Public user representation. Never exposes {@code passwordHash}.
 */
public record UserDto(
        UUID id,
        String email,
        String fullName,
        UserRole role
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
}
