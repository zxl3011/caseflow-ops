# Feature 002: Paginated client listing

## Problem

Partners and Lawyers need to identify existing clients before opening a legal matter. Without an internal client directory, they may create duplicate records or cannot select the correct client during case creation.

Client-directory access is separate from matter access. Listing a client's basic contact record does not grant access to that client's cases, documents or internal legal notes.

## User story

As a Partner or Lawyer, I want to view a paginated directory of client records so that I can identify existing clients, access their contact details and select the correct client when creating a legal matter.

## Scope

This slice provides read-only, paginated access to basic client records. It does not add client search, client updates, portal-account linking or access to a client's matters.

## Authorisation

| Role | List clients |
|---|---:|
| Partner | Yes |
| Lawyer | Yes |
| Paralegal | No |
| Client | No |

Unauthenticated requests receive `401 Unauthorized`. Paralegals and Clients receive `403 Forbidden`.

The first MVP allows Partners and Lawyers to view the basic firm-wide directory because both roles can create clients and legal matters. Matter details remain protected by case assignments and client ownership.

## API contract

```http
GET /api/clients?page=0&size=20
Authorization: Bearer <token>
```

The default page is `0` and the default size is `20`. Page size must be between `1` and `100`.

Successful requests return `200 OK` with a stable ordering by client name ascending and client ID ascending.

Example response:

```json
{
  "items": [
    {
      "id": 10,
      "name": "Example Client",
      "email": "client@example.com",
      "phone": "0400 000 000",
      "createdAt": "2026-09-11T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

The response uses explicit response models. It must not serialize `Client` entities or expose portal-user, case, document or internal-note data.

## Acceptance criteria

- A Partner or Lawyer can retrieve the client directory.
- Results use zero-based pagination with default page `0` and default size `20`.
- Results are ordered by name ascending and then ID ascending.
- The response contains `items`, `page`, `size`, `totalElements` and `totalPages`.
- Every item contains only `id`, `name`, `email`, optional `phone` and `createdAt`.
- An empty directory returns `200 OK`, an empty `items` array and zero totals.
- A page beyond the final page returns `200 OK` with an empty `items` array.
- A negative page, a size below `1` or a size above `100` returns `400 Bad Request`.
- An unauthenticated request receives `401 Unauthorized`.
- Paralegals and Clients receive `403 Forbidden`.
- Listing clients does not grant access to their legal matters.

## Out of scope

- searching by client name or email
- arbitrary client sorting
- filtering by client attributes
- viewing a single client record
- updating or deleting clients
- linking clients to portal accounts
- returning case, document or internal legal information

## Test strategy

- API integration tests verify successful Partner and Lawyer access, the response shape and field restrictions.
- Security tests verify `401 Unauthorized` and `403 Forbidden` outcomes.
- Pagination tests verify defaults, stable ordering, page metadata, empty results and invalid parameters.
- Repository behaviour uses Spring Data JPA pagination against PostgreSQL; no database migration is required.

## Definition of done

- Positive, negative and pagination acceptance tests pass.
- Existing backend tests remain green.
- Frontend lint and build remain green.
- Requirements, API and README status documentation reflect the implemented behaviour.
- No credentials, private client data or generated output are committed.
- GitHub Actions passes on the pull request.
