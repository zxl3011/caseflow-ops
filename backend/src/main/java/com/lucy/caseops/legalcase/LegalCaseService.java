package com.lucy.caseops.legalcase;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LegalCaseService {

    private final LegalCaseRepository legalCaseRepository;

    public LegalCaseService(LegalCaseRepository legalCaseRepository) {
        this.legalCaseRepository = legalCaseRepository;
    }

    @Transactional(readOnly = true)
    public LegalCaseResponse getById(Long caseId) {
        return legalCaseRepository.findWithClientById(caseId)
                .map(LegalCaseResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Case not found"
                ));
    }
}
