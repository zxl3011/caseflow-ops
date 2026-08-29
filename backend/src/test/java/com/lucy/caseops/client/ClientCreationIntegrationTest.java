package com.lucy.caseops.client;

import com.lucy.caseops.security.AuthenticatedUser;
import com.lucy.caseops.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void partnerCanCreateClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(authentication(authenticationFor(100L, Role.PARTNER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Example Client  ",
                                  "email": "  CLIENT@example.com  ",
                                  "phone": "  0400 000 000  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        matchesPattern(".*/api/clients/\\d+")
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Example Client"))
                .andExpect(jsonPath("$.email").value("client@example.com"))
                .andExpect(jsonPath("$.phone").value("0400 000 000"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.user").doesNotExist());
    }

    @Test
    void duplicateClientEmailReturnsConflict() throws Exception {
        createClient("duplicate@example.com")
                .andExpect(status().isCreated());

        createClient("DUPLICATE@example.com")
                .andExpect(status().isConflict());
    }

    @Test
    void lawyerCanCreateClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(authentication(authenticationFor(101L, Role.LAWYER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lawyer Client",
                                  "email": "lawyer-client@example.com"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void paralegalCannotCreateClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(authentication(authenticationFor(102L, Role.PARALEGAL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Restricted Client",
                                  "email": "restricted@example.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCannotCreateClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(authentication(authenticationFor(103L, Role.CLIENT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Restricted Client",
                                  "email": "client-restricted@example.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(authentication(authenticationFor(100L, Role.PARTNER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid Email Client",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions createClient(
            String email
    ) throws Exception {
        return mockMvc.perform(post("/api/clients")
                .with(authentication(authenticationFor(100L, Role.PARTNER)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Duplicate Test Client",
                          "email": "%s"
                        }
                        """.formatted(email)));
    }

    private UsernamePasswordAuthenticationToken authenticationFor(
            Long userId,
            Role role
    ) {
        AuthenticatedUser user = new AuthenticatedUser(
                userId,
                "Integration Test User",
                "integration@example.com",
                "password-hash",
                role
        );
        return UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                user.getAuthorities()
        );
    }
}
