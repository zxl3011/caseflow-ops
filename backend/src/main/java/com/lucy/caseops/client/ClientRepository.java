package com.lucy.caseops.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
            SELECT COUNT(client) > 0
            FROM Client client
            WHERE LOWER(client.email) = :normalisedEmail
            """)
    boolean existsByEmailIgnoreCase(
            @Param("normalisedEmail") String normalisedEmail
    );
}
