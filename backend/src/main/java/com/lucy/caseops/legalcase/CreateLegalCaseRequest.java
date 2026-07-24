package com.lucy.caseops.legalcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLegalCaseRequest(
        @NotNull
        Long clientId,

        @NotBlank
        @Size(max = 50)
        String caseType,

        LocalDate filingDate,

        @Size(max = 150)
        String court,

        LocalDate statuteLimitationDate,

        @Size(max = 5000)
        String description
) {
}
