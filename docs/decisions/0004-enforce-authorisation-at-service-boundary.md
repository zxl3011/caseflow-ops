# ADR-0004: Enforce authorisation at the service boundary

- **Status:** Accepted
- **Date:** 2026-07-24

## Context

Controller-only security protects current HTTP routes but does not protect a service when it is later called from another controller, scheduled job or delivery mechanism.

## Decision

Apply method authorisation to application service methods. Keep target-user compatibility, duplicate assignment and date validation inside the transactional service.

## Consequences

- application use cases remain protected regardless of the caller
- controllers stay focused on HTTP validation and response mapping
- method-security tests must invoke the proxied Spring service
- policies must remain efficient because they may add database queries before the transaction body
