package com.lucy.caseops.legalcase;

import java.time.LocalDateTime;

public record CaseAssignmentResponse(
        Long id,
        Long caseId,
        Long userId,
        String fullName,
        String assignmentRole,
        Long assignedByUserId,
        LocalDateTime assignedAt
) {
    public static CaseAssignmentResponse from(CaseAssignment assignment) {
        return new CaseAssignmentResponse(
                assignment.getId(),
                assignment.getLegalCase().getId(),
                assignment.getUser().getId(),
                assignment.getUser().getFullName(),
                assignment.getAssignmentRole().name(),
                assignment.getAssignedBy().getId(),
                assignment.getAssignedAt()
        );
    }
}
