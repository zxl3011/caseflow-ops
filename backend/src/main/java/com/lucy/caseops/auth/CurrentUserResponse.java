package com.lucy.caseops.auth;

import com.lucy.caseops.security.AuthenticatedUser;

public record CurrentUserResponse(
        Long userId,
        String fullName,
        String email,
        String role
) {
    public static CurrentUserResponse from(AuthenticatedUser user) {
        return new CurrentUserResponse(
                user.id(),
                user.fullName(),
                user.email(),
                user.role().name()
        );
    }
}
