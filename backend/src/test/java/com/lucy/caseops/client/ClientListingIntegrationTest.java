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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void partnerCanListClientsWithDefaultPagination() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .with(authentication(authenticationFor(
                                100L,
                                Role.PARTNER
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    void lawyerCanListClients() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .with(authentication(authenticationFor(101L, Role.LAWYER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void paralegalCannotListClients() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .with(authentication(authenticationFor(102L, Role.PARALEGAL))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCannotListClients() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .with(authentication(authenticationFor(103L, Role.CLIENT))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void emptyDirectoryReturnsEmptyItems() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void pageBeyondFinalPageReturnsEmptyItems() throws Exception {
        createClient("beyond-page@example.com");

        mockMvc.perform(get("/api/clients")
                        .param("page", "5")
                        .param("size", "20")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void resultsAreOrderedByNameThenId() throws Exception {
        createClient("Zed Client", "zed@example.com");
        createClient("Ann Client", "ann-1@example.com");
        createClient("Ann Client", "ann-2@example.com");

        mockMvc.perform(get("/api/clients")
                        .param("page", "0")
                        .param("size", "20")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email").value("ann-1@example.com"))
                .andExpect(jsonPath("$.items[1].email").value("ann-2@example.com"))
                .andExpect(jsonPath("$.items[2].email").value("zed@example.com"));
    }

    @Test
    void negativePageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("page", "-1")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeBelowMinimumReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("size", "0")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeAboveMaximumReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("size", "101")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemsDoNotExposeUserField() throws Exception {
        createClient("dto-safety@example.com");

        mockMvc.perform(get("/api/clients")
                        .with(authentication(authenticationFor(100L, Role.PARTNER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].user").doesNotExist());
    }

    private void createClient(String email) throws Exception {
        createClient("Directory Client", email);
    }

    private void createClient(String name, String email) throws Exception {
        mockMvc.perform(post("/api/clients")
                .with(authentication(authenticationFor(100L, Role.PARTNER)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "%s",
                          "email": "%s"
                        }
                        """.formatted(name, email)))
                .andExpect(status().isCreated());
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
