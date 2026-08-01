# Project overview

## Problem

Legal teams coordinate clients, matters, deadlines, staff and confidential documents. A workflow system must do more than authenticate users: it must ensure that each person can access only the matters and actions appropriate to their role and relationship to the matter.

CaseFlow Ops explores that problem through a small, testable full-stack application.

## Intended users

- **Partner** — oversees matters and staffing across the firm.
- **Lawyer** — creates and leads assigned matters.
- **Paralegal** — supports matters to which they are assigned.
- **Client** — views only their own permitted matter information.

`Partner`, `Lawyer` and `Paralegal` are application security roles. More detailed employment titles such as Associate or Senior Associate are deliberately outside the current security model.

## Product goals

- demonstrate secure authentication and authorisation
- model real relationships between legal staff, clients and matters
- make business rules explicit and testable
- preserve database history through immutable migrations
- provide a portfolio project that can be explained honestly in an interview

## Current scope

The implemented backend supports:

- login and JWT issuance
- authenticated current-user lookup
- case access based on role, assignment or client ownership
- creation of Draft cases by Partners and Lawyers
- staff assignment with role compatibility validation
- safe shared case responses that exclude internal descriptions

The frontend is not yet connected to these capabilities.

## Non-goals

The current version is not:

- a production legal practice management system
- a substitute for legal advice
- a multi-tenant SaaS platform
- integrated with courts, trust accounting or real client data
- a complete document management system

## Delivery approach

Development is organised as small vertical slices:

1. authentication foundation
2. case viewing authorisation
3. Draft case creation and staff assignment
4. client management and complete manual workflows
5. status transitions, documents, deadlines and audit events

Each slice should include:

- acceptance rules
- implementation
- negative authorisation tests
- database migration where required
- local verification
- one focused Git commit
- documentation updates when behaviour or architecture changes
