# Glossary - Projile

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

## Projects

| Term | Meaning |
|---|---|
| **Project** | A body of work inside a tenant; the container for members, boards and tasks. |
| **Project Identifier** | Short uppercase key (`PROJ`, `MKTG`; 4–10 chars). Unique **per tenant** — two tenants may both own `PROJ`. Destined for human references like `PROJ-42` and branch names. |
| **Project Status** | `IN_PROGRESS` (default), `COMPLETED`, `ARCHIVED`. |
| **Project Member** | The link between a user and a project, carrying a `Project Role`. Unique per `(project, user)` pair. |
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