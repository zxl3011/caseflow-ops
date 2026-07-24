package com.lucy.caseops.legalcase;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {

    @EntityGraph(attributePaths = "client")
    Optional<LegalCase> findWithClientById(Long id);

    @Query("""
            SELECT COUNT(legalCase) > 0
            FROM LegalCase legalCase
            WHERE legalCase.id = :caseId
              AND legalCase.client.user.id = :userId
            """)
    boolean isOwnedByClientUser(
            @Param("caseId") Long caseId,
            @Param("userId") Long userId
    );
}
