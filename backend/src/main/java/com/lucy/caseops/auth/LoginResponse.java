package com.lucy.caseops.auth;

public record LoginResponse(
        String token,
        Long userId,
        String fullName,
        String email,
        String role
) {
}