package com.lucy.caseops.legalcase;

import com.lucy.caseops.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "case_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_case_assignments_case_user",
                columnNames = {"legal_case_id", "user_id"}
        )
)
public class CaseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "legal_case_id", nullable = false)
    private LegalCase legalCase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", nullable = false, length = 30)
    private AssignmentRole assignmentRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    protected CaseAssignment() {
    }

    public Long getId() {
        return id;
    }

    public LegalCase getLegalCase() {
        return legalCase;
    }

    public User getUser() {
        return user;
    }

    public AssignmentRole getAssignmentRole() {
        return assignmentRole;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
