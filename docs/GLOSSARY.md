# Glossary — Projile

> Ubiquitous language for the multi-tenant Project Management SaaS.
> Use these terms consistently in code, API, database, docs and UI.
> Keep this file current as new concepts appear (see `guide/best-practices.md`).

---

## Tenancy

| Term | Meaning |
|---|---|
| **Tenant** | An isolated workspace (organization). Everything user-visible lives inside exactly one tenant. Root of the multi-tenancy model; owns members and projects. |
| **Tenant Name** | Human-readable display name of a tenant (2–80 characters). Changeable; the slug is not. |
| **Tenant Slug** | Unique, URL-safe handle (`acme`, `acme-corp`): lowercase letters, digits and single hyphens (3–40 chars). Reserved for subdomain routing later (`acme.projile.app`). |
| **Tenant Status** | Lifecycle state: `ACTIVE`, `SUSPENDED` (blocked/read-only), `ARCHIVED`. |
| **Tenant Member** | The membership linking a `User` to a `Tenant`. Unique per `(tenant, user)` pair. |
| **Tenant Role** | A member's authority *inside the workspace*: `OWNER` (billing, deletion), `ADMIN` (settings, members, projects), `MEMBER` (participates in projects). |
| **Tenant Member Status** | `INVITED` → `ACTIVE` → `SUSPENDED`. Only `ACTIVE` members may act. |
| **Tenant Context** | The tenant resolved for the current request from the authenticated principal plus a verified membership — never from an arbitrary request parameter. Module services require it for tenant-scoped work. |
| **Tenant Isolation** | The non-negotiable invariant: no read or write crosses tenant boundaries. Cross-tenant access answers `404` (not `403`) so resource existence is not leaked. |

## Identity & access

| Term | Meaning |
|---|---|
| **User** | A person with credentials (`email`, `username`, hashed password). Global, not tenant-scoped; may belong to several tenants through memberships. |
| **Username** | Unique, validated handle used for display and login. |
| **Principal** | The authenticated identity attached to a request (`UserPrincipal`); source of the user id when resolving tenant context. |
| **Access token / Refresh token** | Short-lived bearer JWT plus rotating refresh token. *Planned — not implemented yet (see `guide/suggestions.md` Phase 1).* |

## Projects

| Term | Meaning |
|---|---|
| **Project** | A body of work inside a tenant; the container for members, boards and tasks. |
| **Project Identifier** | Short uppercase key (`PROJ`, `MKTG`; 4–10 chars). Unique **per tenant** — two tenants may both own `PROJ`. Destined for human references like `PROJ-42` and branch names. |
| **Project Status** | `IN_PROGRESS` (default), `COMPLETED`, `ARCHIVED`. |
| **Project Member** | The link between a user and a project, carrying a `Project Role`. Unique per `(project, user)` pair. |
| **Project Role** | `OWNER`, `MANAGER`, `MEMBER`, `VIEWER` — see the permission matrix below. |
| **Project Invitation** | An offer to join a project, sent by an existing member to a user. Lifecycle: `PENDING → ACCEPTED / REJECTED / CANCELLED / EXPIRED`; expires 7 days after creation. |

## Work management

| Term | Meaning |
|---|---|
| **Board** | The visual surface of a project. One board per project for now; the model allows several later. |
| **Column** | An ordered lane within a board ("To Do", "In Progress", "Done"). A task's column **is its workflow state** — there is no separate task status. |
| **Position** | Fractional index (LexoRank-style string such as `0|hzzzzz:`) ordering columns within a board and tasks within a column. Inserting between two neighbours never rewrites the other rows. |
| **Task** | A unit of work in a column: title, description, priority, assignee, due date and position. |
| **Task Priority** | `LOW`, `MEDIUM` (default), `HIGH`, `URGENT`. |
| **Assignee** | The user responsible for a task, referenced by `UserId`; optional. |
| **Due date** | Optional moment by which a task should be finished. |
| **Completed** | A task counts as completed when `completedAt` is set (`complete()`); `reopen()` clears it. Independent from which column the task sits in. |
| **Comment** | Free-text note on a task, authored by a user; editable (tracked via `editedAt`). |
| **Attachment** | File metadata (name, content type, size) bound to a task. The binary itself lives in S3-compatible object storage under a `storage key`. |

## Architecture vocabulary

| Term | Meaning |
|---|---|
| **Module** | A bounded context inside the modular monolith: `auth`, `user`, `tenant`, `project`, `board`, `task`, `attachment`, `shared`. Each owns its domain model and persistence. |
| **Public API** | A module's root package: interfaces, typed IDs, views and events. Everything under `internal/` is private to the module. |
| **View (DTO)** | Immutable read model crossing a module or HTTP boundary (e.g. `TaskView`, `BoardView`). Never a JPA entity; internal enums are exposed as strings. |
| **Aggregate / Aggregate root** | Consistency boundary persisted within one transaction. Other aggregates are referenced **by ID only** — never by entity graphs. |
| **Value Object** | Immutable, self-validating domain value persisted through an `AttributeConverter` (`TenantSlug`, `Position`, `Description`, `ProjectIdentifier`, ...). |
| **Domain Event** | Past-tense, immutable record describing something that happened (`TaskMoved`, `MemberJoined`); the asynchronous integration mechanism between modules. |
| **Event publication** | The Spring Modulith table (changeset `001`) registering each published event so failed deliveries can be republished. |
| **UUIDv7 / time-ordered UUID** | Identifiers minted in the domain via `UuidCreator.getTimeOrderedEpoch()`: index-friendly, non-enumerable, no sequences. |
| **Liquibase changeset** | One append-only, numbered migration file. Liquibase owns the schema; Hibernate runs with `ddl-auto=validate` in every profile. |
| **Read projection** | Query-side shape optimized for reads (views, derived repository projections) instead of loading entity graphs. |

## Statuses at a glance

| Concept | Values |
|---|---|
| Tenant status | `ACTIVE`, `SUSPENDED`, `ARCHIVED` |
| Tenant member status | `INVITED`, `ACTIVE`, `SUSPENDED` |
| Tenant role | `OWNER`, `ADMIN`, `MEMBER` |
| Project status | `IN_PROGRESS`, `COMPLETED`, `ARCHIVED` |
| Project role | `OWNER`, `MANAGER`, `MEMBER`, `VIEWER` |
| Invitation status | `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `EXPIRED` |
| Task priority | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |

## Roles & permissions (target behaviour)

Enforcement arrives with the authorization layer (see `guide/suggestions.md`, Phase 3). Until then the UI hides actions, but the **service layer is the security boundary**.

| Action | OWNER | MANAGER | MEMBER | VIEWER |
|---|---|---|---|---|
| View project, board and tasks | ✅ | ✅ | ✅ | ✅ |
| Create / edit / move tasks, comment | ✅ | ✅ | ✅ | — |
| Manage members and invitations | ✅ | ✅ | — | — |
| Edit project (rename, status, archive) | ✅ | ✅ | — | — |
| Delete / transfer project | ✅ | — | — | — |

| Action | Tenant OWNER | Tenant ADMIN | Tenant MEMBER |
|---|---|---|---|
| Billing, plan, delete workspace | ✅ | — | — |
| Tenant settings, members, roles | ✅ | ✅ | — |
| Create projects | ✅ | ✅ | ✅ |

---

*See also: `guide/best-practices.md` (conventions), `guide/features-list.md` (backlog), `guide/implementations.md` (what has been built).*
