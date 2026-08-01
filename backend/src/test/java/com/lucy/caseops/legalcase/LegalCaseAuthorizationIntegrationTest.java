package com.lucy.caseops.legalcase;

import com.lucy.caseops.security.AuthenticatedUser;
import com.lucy.caseops.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LegalCaseAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long caseId;
    private Long clientId;
    private Long partnerId;
    private Long assignedLawyerId;
    private Long unassignedLawyerId;
    private Long unassignedParalegalId;
    private Long owningClientUserId;
    private Long otherClientUserId;

    @BeforeEach
    void setUpCaseAccessRelationships() {
        String testId = UUID.randomUUID().toString();

        partnerId = insertUser("Test Partner", "partner-" + testId, Role.PARTNER);
        assignedLawyerId = insertUser("Assigned Lawyer", "lawyer-" + testId, Role.LAWYER);
        unassignedLawyerId = insertUser(
                "Unassigned Lawyer",
                "other-lawyer-" + testId,
                Role.LAWYER
        );
        unassignedParalegalId = insertUser(
                "Unassigned Paralegal",
                "paralegal-" + testId,
                Role.PARALEGAL
        );
        owningClientUserId = insertUser(
                "Owning Client",
                "client-owner-" + testId,
                Role.CLIENT
        );
        otherClientUserId = insertUser(
                "Other Client",
                "client-other-" + testId,
                Role.CLIENT
        );

        clientId = jdbcTemplate.queryForObject(
                """
                        INSERT INTO clients (name, email, user_id)
                        VALUES (?, ?, ?)
                        RETURNING id
                        """,
                Long.class,
                "Owning Client",
                "client-record-" + testId + "@example.com",
                owningClientUserId
        );

        caseId = jdbcTemplate.queryForObject(
                """
                        INSERT INTO legal_cases (
                            case_number,
                            case_type,
                            status,
                            filing_date,
                            client_id,
                            description
                        )
                        VALUES (?, 'CIVIL', 'OPEN', ?, ?, 'Integration test case')
                        RETURNING id
                        """,
                Long.class,
                "CASE-" + testId,
                LocalDate.now(),
                clientId
        );

        jdbcTemplate.update(
                """
                        INSERT INTO case_assignments (
                            legal_case_id,
                            user_id,
                            assignment_role
                        )
                        VALUES (?, ?, 'LEAD_LAWYER')
                        """,
                caseId,
                assignedLawyerId
        );
    }

    @Test
    void partnerCanViewAnExistingCase() throws Exception {
        mockMvc.perform(get("/api/cases/{caseId}", caseId)
                        .with(authentication(authenticationFor(900L, Role.PARTNER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caseId))
                .andExpect(jsonPath("$.clientName").value("Owning Client"));
    }

    @Test
    void assignedLawyerCanViewCase() throws Exception {
        mockMvc.perform(get("/api/cases/{caseId}", caseId)
                        .with(authentication(authenticationFor(
                                assignedLawyerId,
                                Role.LAWYER
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void unassignedParalegalCannotViewCase() throws Exception {
        mockMvc.perform(get("/api/cases/{caseId}", caseId)
                        .with(authentication(authenticationFor(
                                unassignedParalegalId,
                                Role.PARALEGAL
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void owningClientCanViewCase() throws Exception {
        mockMvc.perform(get("/api/cases/{caseId}", caseId)
                        .with(authentication(authenticationFor(
                                owningClientUserId,
                                Role.CLIENT
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void anotherClientCannotViewCase() throws Exception {
        mockMvc.perform(get("/api/cases/{caseId}", caseId)
                        .with(authentication(authenticationFor(
                                otherClientUserId,
                                Role.CLIENT
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void lawyerCanCreateDraftCase() throws Exception {
        mockMvc.perform(post("/api/cases")
                        .with(authentication(authenticationFor(
                                assignedLawyerId,
                                Role.LAWYER
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "caseType": "civil",
                                  "description": "Internal draft notes"
                                }
                                """.formatted(clientId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.caseType").value("CIVIL"))
                .andExpect(jsonPath("$.caseNumber").value(
                        org.hamcrest.Matchers.matchesPattern(
                                "CF-\\d{4}-[A-F0-9]{8}"
                        )
                ))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void paralegalCannotCreateCase() throws Exception {
        mockMvc.perform(post("/api/cases")
                        .with(authentication(authenticationFor(
                                unassignedParalegalId,
                                Role.PARALEGAL
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": %d,
                                  "caseType": "CIVIL"
                                }
                                """.formatted(clientId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void partnerCanAssignLeadLawyer() throws Exception {
        mockMvc.perform(post("/api/cases/{caseId}/assignments", caseId)
                        .with(authentication(authenticationFor(partnerId, Role.PARTNER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "assignmentRole": "LEAD_LAWYER"
                                }
                                """.formatted(unassignedLawyerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(unassignedLawyerId))
                .andExpect(jsonPath("$.assignmentRole").value("LEAD_LAWYER"));
    }

    @Test
    void leadLawyerCanAssignParalegal() throws Exception {
        mockMvc.perform(post("/api/cases/{caseId}/assignments", caseId)
                        .with(authentication(authenticationFor(
                                assignedLawyerId,
                                Role.LAWYER
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "assignmentRole": "PARALEGAL"
                                }
                                """.formatted(unassignedParalegalId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentRole").value("PARALEGAL"));
    }

    @Test
    void unassignedLawyerCannotAssignUsers() throws Exception {
        mockMvc.perform(post("/api/cases/{caseId}/assignments", caseId)
                        .with(authentication(authenticationFor(
                                unassignedLawyerId,
                                Role.LAWYER
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "assignmentRole": "PARALEGAL"
                                }
                                """.formatted(unassignedParalegalId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void leadLawyerCannotAssignAnotherLeadLawyer() throws Exception {
        mockMvc.perform(post("/api/cases/{caseId}/assignments", caseId)
                        .with(authentication(authenticationFor(
                                assignedLawyerId,
                                Role.LAWYER
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "assignmentRole": "LEAD_LAWYER"
                                }
                                """.formatted(unassignedLawyerId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsAssignmentWhenUserRoleDoesNotMatch() throws Exception {
        mockMvc.perform(post("/api/cases/{caseId}/assignments", caseId)
                        .with(authentication(authenticationFor(partnerId, Role.PARTNER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "assignmentRole": "PARALEGAL"
                                }
                                """.formatted(unassignedLawyerId)))
                .andExpect(status().isBadRequest());
    }

    private Long insertUser(String fullName, String emailPrefix, Role role) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO users (full_name, email, password_hash, role)
                        VALUES (?, ?, 'not-used-by-this-test', ?)
                        RETURNING id
                        """,
                Long.class,
                fullName,
                emailPrefix + "@example.com",
                role.name()
        );
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
