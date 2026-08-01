# ADR-0003: Keep Flyway migrations immutable

- **Status:** Accepted
- **Date:** 2026-07-24

## Context

Flyway records a checksum for each applied migration. Editing a migration after one environment has executed it produces checksum failures and makes environments difficult to reproduce.

## Decision

Treat every applied migration as immutable. Correct or extend the schema with the next numbered migration.

## Consequences

- schema history remains reviewable
- local, test and future deployed environments follow the same sequence
- V1, V2 and V3 must not be rewritten
- even small corrections require a new migration
