# API design

Base URL for local development:

```text
http://localhost:8080
```

Protected endpoints require:

```http
Authorization: Bearer <token>
```

## Endpoint summary

| Method | Path | Authentication | Authorisation |
|---|---|---|---|
| `GET` | `/api/health` | Public | None |
| `POST` | `/api/auth/login` | Public | Valid credentials |
| `GET` | `/api/auth/me` | Required | Any authenticated user |
| `POST` | `/api/clients` | Required | Partner or Lawyer |
| `GET` | `/api/cases/{caseId}` | Required | Partner, assigned staff or owning Client |
| `POST` | `/api/cases` | Required | Partner or Lawyer |
| `POST` | `/api/cases/{caseId}/assignments` | Required | Partner or Lead Lawyer within assignment limits |

## Authentication

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "lawyer@example.com",
  "password": "password123"
}
```

Successful response:

```json
{
  "token": "<signed-jwt>",
  "userId": 2,
  "fullName": "Demo Lawyer",
  "email": "lawyer@example.com",
  "role": "LAWYER"
}
```

Invalid credentials return:

```http
401 Unauthorized
```

### Current user

```http
GET /api/auth/me
Authorization: Bearer <token>
```

The response includes `userId`, `fullName`, `email` and `role`.

## Clients

### Create a client

```http
POST /api/clients
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "Example Client",
  "email": "client@example.com",
  "phone": "0400 000 000"
}
```

Partners and Lawyers receive `201 Created` with a `Location` header and a client-safe response. Text fields are trimmed, email is stored in lowercase, and blank phone input becomes `null`. Paralegals and Clients receive `403 Forbidden`.

Duplicate email is checked case-insensitively. The service performs an early check for a useful response, while a PostgreSQL unique index remains the final concurrency guard. A duplicate returns `409 Conflict`.

## Cases

### View a case

```http
GET /api/cases/42
Authorization: Bearer <token>
```

The service checks:

- Partner — case exists
- Lawyer or Paralegal — user has a case assignment
- Client — case belongs to the linked client record

The shared response excludes the internal `description`.

### Create a Draft case

```http
POST /api/cases
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "clientId": 10,
  "caseType": "civil",
  "filingDate": null,
  "court": "Supreme Court of South Australia",
  "statuteLimitationDate": "2027-07-01",
  "description": "Internal draft notes"
}
```

Response:

```http
201 Created
```

The service:

1. validates the role and dates
2. verifies that the client and creator exist
3. creates a `DRAFT` case with a generated case number
4. assigns the creator as Lead Lawyer
5. commits both writes in one transaction

### Assign staff

```http
POST /api/cases/42/assignments
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "userId": 7,
  "assignmentRole": "PARALEGAL"
}
```

Response:

```http
201 Created
```

Valid assignment roles:

- `LEAD_LAWYER`
- `ASSISTING_LAWYER`
- `PARALEGAL`

## Error semantics

| Status | Meaning |
|---|---|
| `400` | Invalid request or incompatible assignment role |
| `401` | Missing, invalid or expired authentication |
| `403` | Authenticated but not authorised |
| `404` | Referenced client, user or case not found |
| `409` | Duplicate client email or user already assigned to the case |

Security failures use a small JSON structure containing `status`, `error` and `message`. Validation and domain errors currently use Spring Boot's standard error handling; a unified error contract remains planned.

## Planned API improvements

- OpenAPI generation
- client portal linking, listing and update endpoints
- case listing with pagination and role-aware filtering
- controlled case-status transitions
- consistent Problem Details responses
- audit event endpoints
