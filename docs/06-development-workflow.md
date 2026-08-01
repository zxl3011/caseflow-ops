# Development workflow

CaseFlow Ops uses a lightweight software development lifecycle so that each change is understandable, testable and safe to deliver.

## Feature lifecycle

1. Define a user story and measurable acceptance criteria.
2. Identify permissions, validation rules, failure cases and data changes.
3. Record a significant architectural decision in an ADR when appropriate.
4. Add a failing test for the next observable behaviour.
5. Implement the smallest change that makes the test pass.
6. Refactor while keeping the suite green.
7. Run the complete local quality checks.
8. Update affected requirements, API and database documentation.
9. Open a pull request containing the change summary, test evidence and known limitations.
10. Merge only after CI succeeds and review feedback is resolved.

This is a test-driven approach for new business rules, not a claim that every existing line was originally developed with strict TDD.

## Test strategy

- Unit tests isolate service rules and exercise success and failure paths quickly.
- Web-security tests verify authentication filters and HTTP `401`/`403` behaviour.
- Integration tests exercise controllers, Spring Security, transactions, JPA, Flyway and PostgreSQL together.
- Frontend component tests will be introduced with the first functional React vertical slice.

Tests should focus on observable behaviour and risk rather than reproducing implementation details.

## Continuous integration

GitHub Actions runs two independent jobs on every pull request and on pushes to `main`:

- the backend job uses Temurin Java 21 and PostgreSQL 16, then runs the Maven test suite;
- the frontend job uses Node.js 22, installs the locked dependency graph, runs lint and creates a production build.

A failed job blocks the change from meeting the project's definition of done.

## Definition of done

A change is complete when:

- its acceptance criteria are satisfied;
- relevant positive and negative tests pass;
- authorisation and transaction boundaries have been considered;
- the full local quality checks pass;
- affected documentation is current;
- no credentials, private data or generated build output are included;
- CI passes and review feedback is resolved.

## Pull request communication

Each pull request should state:

- the user or operational problem being solved;
- the important implementation and design decisions;
- how the change was verified;
- security, migration or compatibility risks;
- deferred work or known limitations.

This creates a concise engineering record and demonstrates written communication in an asynchronous Agile workflow.
