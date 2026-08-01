package com.lucy.caseops.legalcase;

import com.lucy.caseops.client.Client;
import com.lucy.caseops.client.ClientRepository;
import com.lucy.caseops.security.AuthenticatedUser;
import com.lucy.caseops.user.Role;
import com.lucy.caseops.user.User;
import com.lucy.caseops.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Year;
import java.util.Locale;
import java.util.UUID;

@Service
public class LegalCaseService {

    private final LegalCaseRepository legalCaseRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public LegalCaseService(
            LegalCaseRepository legalCaseRepository,
            CaseAssignmentRepository caseAssignmentRepository,
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.legalCaseRepository = legalCaseRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@caseAuthorization.canView(#caseId, authentication)")
    public LegalCaseResponse getById(Long caseId) {
        return legalCaseRepository.findWithClientById(caseId)
                .map(LegalCaseResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Case not found"
                ));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PARTNER', 'LAWYER')")
    public LegalCaseResponse createDraft(
            CreateLegalCaseRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        if (authenticatedUser.role() != Role.PARTNER
                && authenticatedUser.role() != Role.LAWYER) {
            throw new AccessDeniedException("Only partners and lawyers can create cases");
        }

        validateDates(request.filingDate(), request.statuteLimitationDate());

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client not found"
                ));
        User creator = findUser(authenticatedUser.id());

        LegalCase legalCase = LegalCase.createDraft(
                generateCaseNumber(),
                request.caseType().trim().toUpperCase(Locale.ROOT),
                request.filingDate(),
                trimToNull(request.court()),
                request.statuteLimitationDate(),
                client,
                trimToNull(request.description()),
                creator
        );
        LegalCase savedCase = legalCaseRepository.save(legalCase);

        caseAssignmentRepository.save(CaseAssignment.assign(
                savedCase,
                creator,
                AssignmentRole.LEAD_LAWYER,
                creator
        ));

        return LegalCaseResponse.from(savedCase);
    }

    @Transactional
    @PreAuthorize("""
            @caseAuthorization.canAssign(
                #caseId,
                #request.assignmentRole(),
                authentication
            )
            """)
    public CaseAssignmentResponse assignUser(
            Long caseId,
            CreateCaseAssignmentRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        LegalCase legalCase = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Case not found"
                ));
        User targetUser = findUser(request.userId());
        User assignedBy = findUser(authenticatedUser.id());

        validateAssignmentRole(targetUser, request.assignmentRole());

        if (caseAssignmentRepository.existsByLegalCaseIdAndUserId(
                caseId,
                targetUser.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already assigned to this case"
            );
        }

        CaseAssignment assignment = CaseAssignment.assign(
                legalCase,
                targetUser,
                request.assignmentRole(),
                assignedBy
        );
        try {
            return CaseAssignmentResponse.from(
                    caseAssignmentRepository.saveAndFlush(assignment)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already assigned to this case",
                    exception
            );
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private void validateDates(LocalDate filingDate, LocalDate limitationDate) {
        if (filingDate != null
                && limitationDate != null
                && limitationDate.isBefore(filingDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Statute limitation date cannot be before filing date"
            );
        }
    }

    private void validateAssignmentRole(User targetUser, AssignmentRole assignmentRole) {
        boolean validRole = switch (assignmentRole) {
            case LEAD_LAWYER ->
                    targetUser.getRole() == Role.PARTNER
                            || targetUser.getRole() == Role.LAWYER;
            case ASSISTING_LAWYER -> targetUser.getRole() == Role.LAWYER;
            case PARALEGAL -> targetUser.getRole() == Role.PARALEGAL;
        };

        if (!validRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User role is incompatible with assignment role"
            );
        }
    }

    private String generateCaseNumber() {
        String randomSuffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return "CF-" + Year.now().getValue() + "-" + randomSuffix;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
