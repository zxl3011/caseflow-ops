package com.lucy.caseops.auth;

import com.lucy.caseops.security.AuthenticatedUser;
import com.lucy.caseops.security.JwtService;
import com.lucy.caseops.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService authService = new AuthService(authenticationManager, jwtService);

    @Test
    void returnsTokenAndUserDetailsForValidCredentials() {
        AuthenticatedUser user = new AuthenticatedUser(
                7L,
                "Priya Paralegal",
                "paralegal@example.com",
                "password-hash",
                Role.PARALEGAL
        );
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                user.getAuthorities()
        );
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn("signed-token");

        LoginResponse response = authService.login(
                new LoginRequest("paralegal@example.com", "password123")
        );

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.role()).isEqualTo("PARALEGAL");
    }

    @Test
    void doesNotIssueTokenForInvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("unknown@example.com", "wrong-password")
        )).isInstanceOf(BadCredentialsException.class);
    }
}
