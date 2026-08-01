# Architecture decision records

Architecture decision records explain important choices that are not obvious from the code.

| ADR | Status | Decision |
|---|---|---|
| [0001](0001-reload-users-during-jwt-authentication.md) | Accepted | Reload the current user when authenticating a JWT |
| [0002](0002-combine-roles-with-case-relationships.md) | Accepted | Combine application roles with case relationships |
| [0003](0003-keep-flyway-migrations-immutable.md) | Accepted | Never edit an applied Flyway migration |
| [0004](0004-enforce-authorisation-at-service-boundary.md) | Accepted | Protect application services, not only controllers |
| [0005](0005-use-client-safe-response-models.md) | Accepted | Keep internal fields out of shared client responses |

New ADRs should be short and use:

```text
Title
Status
Context
Decision
Consequences
```
