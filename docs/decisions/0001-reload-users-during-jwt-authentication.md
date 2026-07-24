# ADR-0001: Reload users during JWT authentication

- **Status:** Accepted
- **Date:** 2026-07-24

## Context

A signed JWT can carry a user's role, but that claim can become stale if the user is deleted or their role changes before the token expires.

## Decision

After validating the JWT signature and expiry, use its subject to reload the current user from PostgreSQL. Build the Spring Security principal and authorities from the current database record.

## Consequences

- deleted users immediately lose access
- role changes take effect on the next request
- authorisation uses current data rather than trusting a stale role claim
- every authenticated request performs a user lookup
- a future high-scale version may need short-lived caching or token-version checks
