package com.lucy.caseops.legalcase;

import jakarta.validation.constraints.NotNull;

public record CreateCaseAssignmentRequest(
        @NotNull
        Long userId,

        @NotNull
        AssignmentRole assignmentRole
) {
}
