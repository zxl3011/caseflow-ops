package com.lucy.caseops.legalcase;

import com.lucy.caseops.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
public class LegalCaseController {

    private final LegalCaseService legalCaseService;

    public LegalCaseController(LegalCaseService legalCaseService) {
        this.legalCaseService = legalCaseService;
    }

    @GetMapping("/{caseId}")
    public LegalCaseResponse getById(@PathVariable Long caseId) {
        return legalCaseService.getById(caseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LegalCaseResponse createDraft(
            @Valid @RequestBody CreateLegalCaseRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return legalCaseService.createDraft(request, authenticatedUser);
    }

    @PostMapping("/{caseId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public CaseAssignmentResponse assignUser(
            @PathVariable Long caseId,
            @Valid @RequestBody CreateCaseAssignmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return legalCaseService.assignUser(caseId, request, authenticatedUser);
    }
}
