package com.lucy.caseops.legalcase;

import com.lucy.caseops.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("caseAuthorization")
public class CaseAuthorizationService {

    private final LegalCaseRepository legalCaseRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;

    public CaseAuthorizationService(
            LegalCaseRepository legalCaseRepository,
            CaseAssignmentRepository caseAssignmentRepository
    ) {
        this.legalCaseRepository = legalCaseRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
    }

    public boolean canView(Long caseId, Authentication authentication) {
        if (caseId == null
                || authentication == null
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)
                || user.id() == null) {
            return false;
        }

        return switch (user.role()) {
            case PARTNER -> legalCaseRepository.existsById(caseId);
            case LAWYER, PARALEGAL ->
                    caseAssignmentRepository.existsByLegalCaseIdAndUserId(caseId, user.id());
            case CLIENT -> legalCaseRepository.isOwnedByClientUser(caseId, user.id());
        };
    }
}
