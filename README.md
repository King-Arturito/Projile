# Projile

Projile is a project and task management application being built around workspaces, projects, and Kanban boards. It combines an Angular frontend with a Java backend organized as a modular monolith using Spring Modulith.

**Status: in development.** The current frontend includes a public landing page and an interactive board demo saved in the browser. The backend contains the domain model, database migrations, registration, user lookup, and project lookup foundations. Connecting the board to authenticated, shared workspaces is still in progress.

## Current scope

| Area | Current implementation |
| --- | --- |
| Landing page | Responsive page with feature sections and an embedded board demo |
| Demo board | Create a local board, add and edit tasks, move and reorder cards, search titles, filter labels, and switch between board and list views |
| Local persistence | Demo board state saved in `localStorage`; creating a new board replaces the current demo board |
| Users | Registration and username lookup endpoints, username generation, and password value objects |
| Projects | Project lookup scoped by project and tenant IDs |
| Domain foundation | Entities and repositories for tenants, memberships, projects, invitations, boards, columns, tasks, comments, and attachment metadata |
| Architecture | Spring Modulith structure verification and generated module documentation |

The demo works independently of the backend and uses sample members and task data. It does not synchronize between users or devices.

Login and authenticated workspace flows are unfinished. The current backend security configuration permits `/api/**`, and tenant IDs are still supplied to the project lookup endpoint. Full tenant isolation and authorization remain development work. API-backed frontend screens also need HTTP provider configuration and further integration.

## Technology

| Layer | Stack |
| --- | --- |
| Frontend | Angular 22, TypeScript 6, Angular CDK and Material, SCSS, RxJS |
| Backend | Java 21, Spring Boot 3.5, Spring Modulith 1.4 |
| Persistence | PostgreSQL, Spring Data JPA, Liquibase |
| Security foundation | Spring Security and Argon2 password encoding |
| Testing | JUnit, Mockito, Spring Modulith Test, Vitest, jsdom |
| Build tools | npm, Angular CLI, Maven wrapper |

Dependency versions are defined in [frontend/package.json](frontend/package.json) and [projile/pom.xml](projile/pom.xml).

## Run the frontend demo

### Requirements

- Node.js `^22.22.3`, `^24.15.0`, or `>=26.0.0`, matching the Angular CLI requirement in the lockfile.
- npm; the frontend declares `npm@10.9.4` as its package manager.

### 1. Clone and install

```bash
git clone https://github.com/King-Arturito/Projile.git
cd Projile/frontend
npm ci
```

### 2. Add local environment files

The current source imports environment files that are absent from Git. Create `src/environments/` and add both files below before serving or building the frontend.

`src/environments/environment.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8090/api',
};
```

`src/environments/environment.dev.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8090/api',
};
```

The development build replaces `environment.ts` with `environment.dev.ts`. These values are public client configuration; use an appropriate API origin for your own deployment.

### 3. Start the frontend

```bash
npm start
```

Open [http://localhost:4200](http://localhost:4200) and try the embedded demo. Its board data stays in that browser's local storage. The backend is optional for this demo.

## Run the backend

### Requirements

- JDK 21.
- PostgreSQL with a database and credentials for this application.

The Maven wrapper is included. Commands below assume Bash; on Windows, use `mvnw.cmd` and your shell's environment-variable syntax.

### 1. Configure the application

Create the database before starting the server. Liquibase creates the schema and includes development user seed data when the `dev` context is selected.

### 2. Start the server

From the repository root:

```bash
cd projile
export DB_USERNAME=your_database_user
export DB_PASSWORD='your_database_password'
./mvnw spring-boot:run
```

Set `DB_URL` too if your PostgreSQL instance uses a different host, port, or database name. With the configuration above, the backend listens on `http://localhost:8090`; CORS currently allows `http://localhost:4200`.

### Defined API endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register using an email address and password |
| `GET` | `/api/users?username={username}` | Look up a user by username |
| `GET` | `/api/projects/{projectId}?tenantId={tenantId}` | Look up a project within a tenant |

These endpoints are the current backend foundation. Board, task, membership, and attachment workflows are not yet exposed as complete application APIs.

## Build and test

Run frontend commands from `frontend/`, after creating the environment files:

```bash
npm run build
npm test -- --watch=false
```

The production frontend build is written under `frontend/dist/frontend/`. Frontend tests include the demo board store and existing components and services.

Run backend commands from `projile/`, with database configuration available:

```bash
./mvnw test
./mvnw clean package
```

Backend tests include username and password value objects, username generation, principal lookup, exception handling, and module structure verification. The full suite includes an application context test that needs the configured PostgreSQL database. `ModularityTests` also generates module documentation under the backend's `target/` directory.

## Project structure

```text
frontend/
  src/app/
    core/                 Authentication and HTTP foundations
    features/
      landing/            Public page and browser-based board demo
      user/               User lookup screen and service
      project/            Project UI and service foundations
      auth/, tenant/,
      board/, task/       Feature scaffolding
    shared/               Reusable UI and icons
  src/styles/             Shared SCSS variables
projile/
  src/main/java/io/github/kingarturito/projile/
    auth/                 Registration and security configuration
    user/                 Users, credentials, and username generation
    tenant/               Workspaces and tenant memberships
    project/              Projects, memberships, and invitations
    board/                Boards and columns
    task/                 Tasks and comments
    attachment/           Attachment metadata
    shared/               Shared value objects and API exceptions
  src/main/resources/db/  Liquibase schema and development seed data
  src/test/               Backend tests and module verification
docs/                     Domain glossary
```

Modules expose IDs, views, and service interfaces, with persistence and implementation details grouped under `internal/`. The [domain glossary](docs/GLOSSARY.md) describes the workspace and work management concepts, including intended behavior still being implemented.

## PS

- This is a personal project & currently unfinished (10/22/26).

## License

Projile is licensed under the [MIT License](LICENSE).
