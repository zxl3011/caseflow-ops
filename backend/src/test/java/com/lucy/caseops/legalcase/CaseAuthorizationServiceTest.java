package com.lucy.caseops.legalcase;

import com.lucy.caseops.security.AuthenticatedUser;
import com.lucy.caseops.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CaseAuthorizationServiceTest {

    private final LegalCaseRepository legalCaseRepository = mock(LegalCaseRepository.class);
    private final CaseAssignmentRepository caseAssignmentRepository =
            mock(CaseAssignmentRepository.class);
    private final CaseAuthorizationService authorizationService =
            new CaseAuthorizationService(legalCaseRepository, caseAssignmentRepository);

    @Test
    void allowsPartnerToViewExistingCase() {
        when(legalCaseRepository.existsById(10L)).thenReturn(true);

        assertThat(authorizationService.canView(10L, authenticationFor(Role.PARTNER)))
                .isTrue();

        verify(legalCaseRepository).existsById(10L);
        verifyNoInteractions(caseAssignmentRepository);
    }

    @Test
    void allowsAssignedLawyerToViewCase() {
        when(caseAssignmentRepository.existsByLegalCaseIdAndUserId(10L, 99L))
                .thenReturn(true);

        assertThat(authorizationService.canView(10L, authenticationFor(Role.LAWYER)))
                .isTrue();
    }

    @Test
    void deniesUnassignedParalegal() {
        when(caseAssignmentRepository.existsByLegalCaseIdAndUserId(10L, 99L))
                .thenReturn(false);

        assertThat(authorizationService.canView(10L, authenticationFor(Role.PARALEGAL)))
                .isFalse();
    }

    @Test
    void allowsClientToViewOwnCase() {
        when(legalCaseRepository.isOwnedByClientUser(10L, 99L)).thenReturn(true);

        assertThat(authorizationService.canView(10L, authenticationFor(Role.CLIENT)))
                .isTrue();
    }

    @Test
    void deniesClientAccessToAnotherClientsCase() {
        when(legalCaseRepository.isOwnedByClientUser(10L, 99L)).thenReturn(false);

        assertThat(authorizationService.canView(10L, authenticationFor(Role.CLIENT)))
                .isFalse();
    }

    @Test
    void allowsPartnerToAssignUsersToExistingCase() {
        when(legalCaseRepository.existsById(10L)).thenReturn(true);

        assertThat(authorizationService.canAssign(
                10L,
                AssignmentRole.LEAD_LAWYER,
                authenticationFor(Role.PARTNER)
        )).isTrue();
    }

    @Test
    void allowsLeadLawyerToAssignSupportingStaff() {
        when(caseAssignmentRepository
                .existsByLegalCaseIdAndUserIdAndAssignmentRole(
                        10L,
                        99L,
                        AssignmentRole.LEAD_LAWYER
                ))
                .thenReturn(true);

        assertThat(authorizationService.canAssign(
                10L,
                AssignmentRole.PARALEGAL,
                authenticationFor(Role.LAWYER)
        )).isTrue();
    }

    @Test
    void deniesLeadLawyerFromAssigningAnotherLeadLawyer() {
        assertThat(authorizationService.canAssign(
                10L,
                AssignmentRole.LEAD_LAWYER,
                authenticationFor(Role.LAWYER)
        )).isFalse();

        verifyNoInteractions(legalCaseRepository, caseAssignmentRepository);
    }

    @Test
    void deniesParalegalFromAssigningUsers() {
        assertThat(authorizationService.canAssign(
                10L,
                AssignmentRole.PARALEGAL,
                authenticationFor(Role.PARALEGAL)
        )).isFalse();

        verifyNoInteractions(legalCaseRepository, caseAssignmentRepository);
    }

    private UsernamePasswordAuthenticationToken authenticationFor(Role role) {
        AuthenticatedUser user = new AuthenticatedUser(
                99L,
                "Test User",
                "test@example.com",
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
