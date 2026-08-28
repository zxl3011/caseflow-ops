# Feature 001: Client creation

## User story

As a Partner or Lawyer, I want to create a client record so that a legal matter can be opened for that client.

## Scope

This slice creates a client record only. Linking the client to a portal user, listing clients and updating client details are separate slices.

## Authorisation

| Role | Create client |
|---|---:|
| Partner | Yes |
| Lawyer | Yes |
| Paralegal | No |
| Client | No |

Paralegals may support client intake in a future workflow, but the first MVP slice reserves formal client creation for Partners and Lawyers.

## API contract

`POST /api/clients`

Example request:

```json
{
  "name": "Example Client",
  "email": "CLIENT@example.com",
  "phone": "0400 000 000"
}
```

Successful creation returns `201 Created`, a `Location` header for the new resource and a client-safe response containing:

- `id`
- `name`
- normalised lowercase `email`
- optional `phone`
- `createdAt`

The response must not serialize the JPA entity or expose a linked portal user.

## Acceptance criteria

- A Partner or Lawyer can create a client with a valid name and email.
- Leading and trailing whitespace is removed from textual fields.
- Email is stored and returned in lowercase.
- Phone is optional and blank phone input is stored as `null`.
- Blank or oversized names, invalid emails and oversized phone values return `400 Bad Request`.
- Paralegals and Clients receive `403 Forbidden`.
- A duplicate email, compared case-insensitively, returns `409 Conflict`.
- Database constraints remain the final protection against concurrent duplicate requests.
- No portal account is created or linked by this operation.

## Test strategy

- API integration tests verify HTTP status, response shape, authorisation and PostgreSQL persistence.
- Service unit tests isolate normalisation and duplicate-handling rules where they add value.
- A database migration will enforce the case-insensitive email invariant rather than relying only on an application pre-check.

## Definition of done

- Positive and negative acceptance tests pass.
- Existing backend tests remain green.
- Frontend lint and build remain green.
- Requirements, API and database documentation reflect the implemented behaviour.
- GitHub Actions passes on the pull request.
