package com.lucy.caseops.legalcase;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseAssignmentRepository extends JpaRepository<CaseAssignment, Long> {

    boolean existsByLegalCaseIdAndUserId(Long legalCaseId, Long userId);

    boolean existsByLegalCaseIdAndUserIdAndAssignmentRole(
            Long legalCaseId,
            Long userId,
            AssignmentRole assignmentRole
    );
}
