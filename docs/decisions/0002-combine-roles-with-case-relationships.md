# ADR-0002: Combine roles with case relationships

- **Status:** Accepted
- **Date:** 2026-07-24

## Context

Pure role-based access would allow every Lawyer or Paralegal to access every case. Legal work requires a narrower boundary: staff should see a case only when they are assigned to it, while Clients should see only their own matters.

## Decision

Use application roles for broad capabilities and database relationships for resource access:

- Partner — firm-wide access in the single-firm MVP
- Lawyer and Paralegal — assignment-based access
- Client — client-ownership access

Represent staff relationships in `case_assignments`, including the assignment role.

## Consequences

- the model supports multiple lawyers and paralegals per case
- authorisation queries must consult the database
- assigning a role does not automatically expose every case
- adding multi-tenancy will require a firm boundary in addition to these checks
