# Requirements and permissions

## Functional requirements

### Authentication

- A user can log in with email and password.
- Passwords are verified using BCrypt.
- A successful login returns a signed, expiring JWT.
- A protected request must include a valid Bearer token.
- A token for a user that no longer exists must not establish authentication.

### Case access

- A Partner can view any existing case in the current single-firm system.
- A Lawyer can view a case only when assigned to it.
- A Paralegal can view a case only when assigned to it.
- A Client can view a case only when the case belongs to their linked client record.
- A shared client-safe response must not expose the internal case description.

### Case creation

- A Partner or Lawyer can create a case in `DRAFT` status.
- A newly created case receives a generated reference such as `CF-2026-AB12CD34`.
- The creator is automatically assigned as `LEAD_LAWYER`.
- A Draft may omit its filing date.
- A limitation date cannot be earlier than its filing date.
- A case must reference an existing client.

### Staff assignment

- A Partner can assign staff to an existing case.
- A Lead Lawyer can assign an Assisting Lawyer or Paralegal.
- A Lead Lawyer cannot appoint another Lead Lawyer.
- Paralegals and Clients cannot assign staff.
- `LEAD_LAWYER` must target a Partner or Lawyer.
- `ASSISTING_LAWYER` must target a Lawyer.
- `PARALEGAL` must target a Paralegal.
- The same user cannot be assigned to the same case twice.

## Permission matrix

| Capability | Partner | Lawyer | Paralegal | Client |
|---|---:|---:|---:|---:|
| View any case | Yes | No | No | No |
| View assigned case | Yes | Yes | Yes | No |
| View own client case | Not applicable | Not applicable | Not applicable | Yes |
| Create Draft case | Yes | Yes | No | No |
| Assign Lead Lawyer | Yes | No | No | No |
| Assign supporting staff | Yes | Lead Lawyer only | No | No |
| View internal description | Not exposed by current API | Not exposed by current API | Not exposed by current API | No |

## Security requirements

- Authentication must be stateless.
- Invalid or absent authentication must return `401`.
- Authenticated users without permission must receive `403`.
- Authorisation must be enforced at the service boundary.
- Authorisation queries must check the current database state.
- Database constraints must protect invariants during concurrent requests.
- API responses must expose only fields appropriate for their audience.
- Secrets and production credentials must not be committed.

## Planned requirements

- Partners can open an approved Draft case.
- Lead Lawyers can update assigned case details.
- Clients can be created and linked to portal accounts.
- Documents have internal and client-visible classifications.
- Deadline and hearing changes produce audit events.
- The frontend provides role-aware navigation and protected routes.
