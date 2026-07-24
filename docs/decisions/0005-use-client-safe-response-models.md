# ADR-0005: Use client-safe response models

- **Status:** Accepted
- **Date:** 2026-07-24

## Context

Object-level permission to view a case does not imply permission to view every case field. An internal description may contain legal strategy or staff notes that should not be exposed to a Client.

## Decision

Return an explicit case response model that excludes the internal description. Do not serialize JPA entities directly.

## Consequences

- the current shared endpoint exposes only intentionally selected fields
- database entities can evolve without silently expanding the public API
- future internal and client views may use separate response models
- field-level visibility must be reviewed whenever a response changes
