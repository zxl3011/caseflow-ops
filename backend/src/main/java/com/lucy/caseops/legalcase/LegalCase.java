package com.lucy.caseops.legalcase;

import com.lucy.caseops.client.Client;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_cases")
public class LegalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @Column(name = "case_type", nullable = false, length = 50)
    private String caseType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "filing_date", nullable = false)
    private LocalDate filingDate;

    @Column(length = 150)
    private String court;

    @Column(name = "statute_limitation_date")
    private LocalDate statuteLimitationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected LegalCase() {
    }

    public Long getId() {
        return id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public String getCaseType() {
        return caseType;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getFilingDate() {
        return filingDate;
    }

    public String getCourt() {
        return court;
    }

    public LocalDate getStatuteLimitationDate() {
        return statuteLimitationDate;
    }

    public Client getClient() {
        return client;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
