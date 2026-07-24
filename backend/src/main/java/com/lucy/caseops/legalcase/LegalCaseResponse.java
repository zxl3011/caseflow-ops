package com.lucy.caseops.legalcase;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LegalCaseResponse(
        Long id,
        String caseNumber,
        String caseType,
        String status,
        LocalDate filingDate,
        String court,
        LocalDate statuteLimitationDate,
        Long clientId,
        String clientName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LegalCaseResponse from(LegalCase legalCase) {
        return new LegalCaseResponse(
                legalCase.getId(),
                legalCase.getCaseNumber(),
                legalCase.getCaseType(),
                legalCase.getStatus(),
                legalCase.getFilingDate(),
                legalCase.getCourt(),
                legalCase.getStatuteLimitationDate(),
                legalCase.getClient().getId(),
                legalCase.getClient().getName(),
                legalCase.getCreatedAt(),
                legalCase.getUpdatedAt()
        );
    }
}
