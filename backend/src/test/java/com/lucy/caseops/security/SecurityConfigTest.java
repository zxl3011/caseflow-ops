package com.lucy.caseops.security;

import com.lucy.caseops.auth.AuthController;
import com.lucy.caseops.auth.AuthExceptionHandler;
import com.lucy.caseops.auth.AuthService;
import com.lucy.caseops.config.SecurityConfig;
import com.lucy.caseops.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        AuthExceptionHandler.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void rejectsProtectedRequestWithInvalidToken() throws Exception {
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesProtectedRequestWithValidToken() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(
                42L,
                "Alex Partner",
                "partner@example.com",
                "password-hash",
                Role.PARTNER
        );
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn(user.email());
        when(userDetailsService.loadUserByUsername(user.email())).thenReturn(user);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.email").value("partner@example.com"))
                .andExpect(jsonPath("$.role").value("PARTNER"));
    }

    @Test
    void rejectsValidTokenWhenUserNoLongerExists() throws Exception {
        when(jwtService.isTokenValid("orphaned-token")).thenReturn(true);
        when(jwtService.extractEmail("orphaned-token")).thenReturn("deleted@example.com");
        when(userDetailsService.loadUserByUsername("deleted@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer orphaned-token"))
                .andExpect(status().isUnauthorized());
    }
}
