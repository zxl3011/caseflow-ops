package com.lucy.caseops.legalcase;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
public class LegalCaseController {

    private final LegalCaseService legalCaseService;

    public LegalCaseController(LegalCaseService legalCaseService) {
        this.legalCaseService = legalCaseService;
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("@caseAuthorization.canView(#caseId, authentication)")
    public LegalCaseResponse getById(@PathVariable Long caseId) {
        return legalCaseService.getById(caseId);
    }
}
