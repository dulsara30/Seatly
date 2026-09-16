# Seatly — Event RSVP & Waitlist Manager

Internship mini-project at Rootcode Labs. The point of it is to practise
production-grade engineering: clean layering, SOLID, proper transactions, real
concurrency handling, no code smells. Code quality is the deliverable, not just
working features.

---

## What the product does

Organisers create events with a seat limit. People RSVP. If seats remain they
are CONFIRMED immediately; if the event is full they are WAITLISTED with a
position. When a confirmed attendee cancels, the first person on the waitlist
is promoted automatically and emailed. Events are ONLINE (meeting link) or
PHYSICAL (venue address), and carry tags for filtering.

Any registered user can create an event and becomes the organiser **of that
event**. The same person is an attendee on other people's events. There is no
global role and no admin.

---

## Stack

| Layer      | Choice                                                         |
| ---------- | -------------------------------------------------------------- |
| Java       | 21                                                             |
| Framework  | Spring Boot **4.1.1**                                          |
| Build      | Maven via wrapper (`./mvnw`) — Maven is not installed globally |
| DB         | PostgreSQL 16, Docker, host port **5433**                      |
| Migrations | Flyway                                                         |
| Mapping    | MapStruct 1.6.3                                                |
| Auth       | JWT (jjwt 0.12.6) + Spring Security                            |
| Docs       | springdoc-openapi **3.1.0**                                    |
| Frontend   | Next.js + TypeScript                                           |
| FE data    | TanStack Query (server state) · Zustand (client state)         |
| FE forms   | Formik + Yup                                                   |
| FE styling | Tailwind                                                       |

**Base package: `com.seatly.backend`**

### Spring Boot 4 specifics — do not get these wrong

Boot 4 is recent and differs from the 3.x material most examples use:

- The web starter is `spring-boot-starter-webmvc`, **not** `spring-boot-starter-web`
- Flyway comes from `spring-boot-starter-flyway`, not a bare `flyway-core`
- Test starters are per-module (`spring-boot-starter-data-jpa-test` etc.), not one `spring-boot-starter-test`
- Boot 4 uses **Jackson 3**. springdoc 2.x is incompatible and fails at startup — 3.1.0 is required
- Spring Security 7, Hibernate 7

If a suggested dependency or API looks like Boot 3, verify it before using it.

---

## Current state

Done: project generated, `pom.xml` compiling, Postgres running in Docker,
`application.yml` configured.

Not started: migrations, entities, everything else.

Immediate goal: **Event CRUD working end-to-end with Swagger.**
RSVP and auth come after.

---

# PART 1 — UNIVERSAL RULES

These apply to backend and frontend equally.

## SOLID

- **Single responsibility** — one class, one hook, one component does one thing.
  If you need "and" to describe it, split it.
- **Open/closed** — extend by adding, not by editing a growing `if/else` or
  `switch`. A chain that keeps growing is a missing polymorphic type.
- **Liskov** — an implementation must be safely substitutable for its interface.
- **Interface segregation** — small focused interfaces. No fat interface where
  implementers throw `UnsupportedOperationException`.
- **Dependency inversion** — depend on interfaces, not concrete classes.
  Controllers depend on `EventService`, never on `EventServiceImpl`.

## Code smells to avoid — non-negotiable

- **No magic numbers or magic strings.** Ever. Named constants, enums, or config.
  `DEFAULT_PAGE_SIZE`, not `20` inline. `EventStatus.UPCOMING`, not `"UPCOMING"`.
- **No fallback values.** No `?? 0`, no `|| []`, no `orElse` inventing a default
  to make something render. Missing data is a bug to surface, not to paper over.
- **No `any`, no raw types, no unchecked casts.** Real types everywhere.
- **No duplication.** Third occurrence, extract it. Second occurrence, note it.
- **No long methods.** If it needs section comments, it needs to be several methods.
- **No deep nesting.** Guard clauses and early returns instead of arrow code.
- **No dead code**, no commented-out blocks, no unused imports. Git remembers.
- **Plain English names.** `confirmedSeatCount`, not `cnt`, `data2`, `tmp`, `flag`.
- **No speculative abstraction.** Build what this feature needs. No generic
  framework for a single caller.
- **No God classes or God components.** A file doing five unrelated things gets split.

## Reuse

- **Utilities** → a `utils/` folder. Pure functions, no side effects, unit tested.
  Date formatting, string helpers, seat-count maths.
- **Hooks (FE)** → a `hooks/` folder. Anything stateful reused across components.
- Copy-pasting a function into a second file is the signal to extract it.

## Constants

No literal ever appears at a call site. Frontend:

```
constants/
├── pagination.ts      DEFAULT_PAGE_SIZE, INFINITE_SCROLL_PAGE_SIZE
├── timing.ts          SEARCH_DEBOUNCE_MS, SSE_HEARTBEAT_MS
├── routes.ts          every app route path
└── messages.ts        every user-facing string
```

Backend: enums for anything with a fixed set of values, `@ConfigurationProperties`
for tunables, `static final` for true constants.

---

# PART 2 — BACKEND

## Architecture

Layered modular monolith. One database, one deployable. No multi-tenancy —
public platform, ownership enforced per row via `organizer_id`.

```
com.seatly.backend
├── common/
│   ├── config/         security, openapi, jackson, async, scheduling
│   ├── constant/       shared constants
│   ├── exception/      typed exceptions + GlobalExceptionHandler
│   ├── payload/        ResponseEntityDto, PageDto
│   ├── model/          Auditable base entity
│   ├── type/           shared enums, message keys
│   └── util/           pure helpers
├── user/               controller, service, repository, model, mapper, payload, type
├── event/              (same seven)
├── rsvp/               (same seven)
├── tag/                (same seven)
└── notification/       email service, outbox job
```

Every domain repeats the same seven folders. Consistency is the architectural
decision — learn one module, navigate all of them.

## Layer contract

| Layer                 | Does                                                                          | Must never do                                              |
| --------------------- | ----------------------------------------------------------------------------- | ---------------------------------------------------------- |
| Controller            | HTTP only: path, verb, status, `@PreAuthorize`, OpenAPI annotations, `@Valid` | Business logic, validation logic, entity access, try/catch |
| Service (interface)   | The contract other layers depend on                                           | —                                                          |
| ServiceImpl           | **All** business rules, all validation logic, `@Transactional`, orchestration | HTTP concerns, raw SQL                                     |
| Dao (`JpaRepository`) | Derived queries (`existsByEmail`)                                             | Complex dynamic filtering                                  |
| RepositoryImpl        | Dynamic queries via Criteria API                                              | Business decisions                                         |
| Mapper (MapStruct)    | Entity ↔ DTO, field-for-field                                                 | Any logic, any conditional                                 |
| Model (`@Entity`)     | JPA entity extending `Auditable`                                              | Business methods                                           |
| DTO                   | Dumb data carrier                                                             | Methods, computation, derived fields                       |

**Controllers are thin to the point of boring.** A controller method is:
receive → delegate to service → wrap in response. Three lines. If there is an
`if` about domain rules in a controller, it is in the wrong layer.

### Two kinds of validation — keep them separate

This distinction matters, don't collapse it:

**Shape validation** lives on the request DTO as Bean Validation annotations —
`@NotNull`, `@Size`, `@Email`, `@Min`. These are declarative constraints, not
logic. The controller applies them with `@Valid` and never writes an `if`.

**Business validation** lives in `ServiceImpl`, always. Anything that needs the
database or domain knowledge:

- new `seatLimit` below current confirmed count
- ONLINE mode requires `meetingLink`, PHYSICAL requires `location`
- event must be UPCOMING to accept an RSVP
- caller must be the organiser
- `eventDate` must be in the future

Rule of thumb: if checking it requires a query or knowledge of another field,
it's business validation and belongs in the service.

## Global infrastructure — build these first

**Global response wrapper.** Every endpoint returns the same shape:

```json
{ "status": "successful", "results": [{}] }
```

Errors:

```json
{
  "status": "unsuccessful",
  "results": [{ "message": "EVENT_ERROR_SEAT_LIMIT_BELOW_CONFIRMED" }]
}
```

Implement as `ResponseEntityDto<T>` in `common/payload`. `results` is always an
array, even for a single object — one shape for the frontend to parse.

**Global exception handler.** One `@RestControllerAdvice` maps typed exceptions
to status + message key. No try/catch anywhere in controllers. Handle at minimum:
`ModuleException` (custom base), `MethodArgumentNotValidException` (Bean
Validation failures), `AccessDeniedException`, `DataIntegrityViolationException`,
and a catch-all `Exception` that logs the stack trace and returns a generic key.
Never leak a stack trace or SQL message in a response body.

**Global string trimmer.** Incoming strings are trimmed everywhere, centrally —
not with `.trim()` scattered through services. Register a Jackson deserializer
module for `String` in `common/config`. Trim to `null` when the result is empty,
so `"   "` does not sneak past a `@NotBlank` check.

**Message keys as enums.** Error messages are keys, never prose:
`EVENT_ERROR_NOT_FOUND`, `RSVP_ERROR_ALREADY_EXISTS`. The frontend maps key →
copy. Keeps display text out of the backend and leaves translation open.

**Auditable base entity.** `@MappedSuperclass` with `createdAt`/`updatedAt`,
filled by Spring Data auditing. Every entity extends it.

**Soft delete.** `is_deleted` flag. Every query filters on it. Nothing is ever
physically removed.

## The proxy trap

`@Transactional`, `@PreAuthorize`, `@Async` work via a proxy. A call from one
method to another **in the same class** bypasses the proxy and the annotation
silently does nothing.

```java
@Transactional                       // on the entry point the controller calls
public void cancelRsvp(Long id) {
    freeSeat(id);
    promoteNextFromWaitlist(id);     // private, inherits this transaction
}
```

Put `@Transactional` on the outermost public method. Never rely on it on a
method called internally.

---

# PART 3 — FRONTEND

## Component architecture — atomic design

```
components/
├── atoms/        Button, Badge, Avatar, DateChip, SeatBar, Input, TagChip
├── molecules/    EventCard, SearchBar, FilterBar, RSVPPanel, WaitlistRow
├── organisms/    EventGrid, AttendeeTable, OrganiserDashboard, Header
├── templates/    page shells, layout composition
└── pages/        route-level components
```

Rules:

- **Atoms never fetch data.** Props only. Pure presentation.
- **Molecules never fetch data.** Composed atoms, still props-driven.
- **Organisms may call API hooks.** This is the first layer allowed to.
- **Templates** handle layout and slots, no data.
- **Pages** wire routing and compose templates.

Placement test: reusable across modules → atom; reusable within one module →
molecule; owns data or orchestrates → organism.

## No heavy prop drilling

Passing a prop through more than **two** layers that don't use it is a smell.

Fix with, in order of preference:

1. Move state down — does the parent actually need it?
2. Composition — pass JSX as children instead of passing data down
3. Zustand — genuinely global client state
4. Context — only for a value that is truly tree-wide and rarely changes

Never reach for Zustand or Context as the first move.

## Types — defined once, in one place

All types live in a single `types/` tree. Defined once, locked in, imported
everywhere. Never redeclare a shape inline, never duplicate a payload type.

```
types/
├── entities/     Event, User, Rsvp, Tag                              (domain shapes)
├── requests/     CreateEventRequest, UpdateEventRequest              (what we send)
└── responses/    EventResponse, PagedResponse<T>, ApiEnvelope<T>     (what we receive)
```

Three separate kinds and they don't get merged:

- **Entity** — the domain object
- **Request payload** — what the API accepts (often a subset)
- **Response payload** — what the API returns (often has derived fields like
  `availableSeats` that no entity has)

Derive rather than duplicate: `Pick`, `Omit`, `Partial`. Response types mirror
the backend DTO exactly — if the backend changes, the type changes in one file.

## API layer — the strictest rule here

**Components never touch the API.** No fetch, no axios, no `queryClient` calls,
no invalidation. Not once. Everything API-shaped lives in the API layer and
components consume hooks.

```
api/
├── utils/
│   ├── ApiEndpoints.ts    every URL, as a const or function
│   └── QueryKeys.ts       every TanStack Query cache key
├── EventApi.ts            fetch fns + the hooks that use them
├── RsvpApi.ts
└── TagApi.ts
```

**Endpoints defined separately.** No URL string ever appears in a hook or component.

```ts
export const ApiEndpoints = {
  event: {
    list: "/v1/events",
    detail: (id: number) => `/v1/events/${id}`,
    rsvp: (id: number) => `/v1/events/${id}/rsvp`,
  },
} as const;
```

**Query keys defined separately.** Cache invalidation needs the exact key used
to fetch. Inline strings make invalidation guesswork.

```ts
export const QueryKeys = {
  event: {
    all: ["events"] as const,
    list: (filters: EventFilters) => ["events", "list", filters] as const,
    detail: (id: number) => ["events", "detail", id] as const,
  },
} as const;
```

**Invalidation lives in the API layer, never in a component.** The mutation hook
owns its own cache consequences:

```ts
export const useCreateRsvp = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createRsvp,
    onSuccess: (_, { eventId }) => {
      queryClient.invalidateQueries({
        queryKey: QueryKeys.event.detail(eventId),
      });
      queryClient.invalidateQueries({ queryKey: QueryKeys.rsvp.my() });
    },
  });
};
```

The component just calls `mutate()`. It doesn't know or care what got
invalidated. Move the API and every consumer keeps working.

## The two-store rule

| Kind of state                           | Tool               | Examples                                    |
| --------------------------------------- | ------------------ | ------------------------------------------- |
| Server state — the backend owns it      | **TanStack Query** | events, RSVPs, attendees, tags              |
| Client state — the server never sees it | **Zustand**        | open modal, unsubmitted filters, UI toggles |

**Never copy fetched data into Zustand.** Query already caches, dedupes and
refetches it. A copy is a second source of truth that goes stale.

## Lists, search, forms

**Pagination / infinite scroll** — `useInfiniteQuery` for the browse grid. Page
size from `constants/pagination.ts`, never a literal.

**Search debounce** — always. `SEARCH_DEBOUNCE_MS` from `constants/timing.ts`
(300ms is the usual). Debounce the value, then feed it into the query key so
TanStack handles cancellation for you.

**Forms — Formik + Yup, no exceptions.** No hand-rolled `useState` form state,
no manual validation. Yup schemas live beside the form or in `validation/`, and
mirror the backend's Bean Validation constraints. The backend still re-validates
everything — frontend validation is UX, never a security boundary.

---

# PART 4 — DATABASE

Six tables. Flyway owns the schema entirely; Hibernate runs `ddl-auto: validate`
and only checks entities match. Never switch that to `update`.

### user

| Column                  | Type         | Notes            |
| ----------------------- | ------------ | ---------------- |
| id                      | BIGINT       | PK               |
| name                    | VARCHAR(100) | not null         |
| email                   | VARCHAR(255) | not null, unique |
| password                | VARCHAR(255) | BCrypt hash      |
| bio                     | VARCHAR(500) | nullable         |
| is_active               | BOOLEAN      | default true     |
| is_deleted              | BOOLEAN      | default false    |
| created_at / updated_at | TIMESTAMP    | auditing         |

### event

| Column                  | Type         | Notes                                        |
| ----------------------- | ------------ | -------------------------------------------- |
| id                      | BIGINT       | PK                                           |
| organizer_id            | BIGINT       | FK → user.id                                 |
| name                    | VARCHAR(255) | not null                                     |
| description             | TEXT         | not null                                     |
| mode                    | VARCHAR(20)  | ONLINE \| PHYSICAL                           |
| location                | VARCHAR(255) | required when PHYSICAL                       |
| meeting_link            | VARCHAR(500) | required when ONLINE                         |
| event_date              | TIMESTAMP    | must be future on create                     |
| seat_limit              | INT          | min 1                                        |
| status                  | VARCHAR(20)  | UPCOMING \| CANCELLED \| COMPLETED           |
| link_sent_at            | TIMESTAMP    | nullable — when the meeting link was emailed |
| is_deleted              | BOOLEAN      | default false                                |
| created_at / updated_at | TIMESTAMP    | auditing                                     |

### rsvp

| Column                  | Type        | Notes                                |
| ----------------------- | ----------- | ------------------------------------ |
| id                      | BIGINT      | PK                                   |
| user_id                 | BIGINT      | FK → user.id                         |
| event_id                | BIGINT      | FK → event.id                        |
| status                  | VARCHAR(20) | CONFIRMED \| WAITLISTED \| CANCELLED |
| position                | INT         | nullable, set only when WAITLISTED   |
| created_at / updated_at | TIMESTAMP   | auditing                             |

Unique constraint `(user_id, event_id)` · index `(event_id, status)`

### tag

`id BIGINT PK` · `name VARCHAR(100) not null unique` — always stored lowercase

### event_tag

`event_id BIGINT FK` · `tag_id BIGINT FK` — composite PK, many-to-many

### email_outbox

| Column         | Type         | Notes                               |
| -------------- | ------------ | ----------------------------------- |
| id             | BIGINT       | PK                                  |
| recipient      | VARCHAR(255) | address copied at queue time        |
| subject        | VARCHAR(255) | not null                            |
| body           | TEXT         | not null                            |
| status         | VARCHAR(20)  | PENDING \| SENT \| FAILED           |
| attempts       | INT          | default 0                           |
| reference_type | VARCHAR(50)  | e.g. EVENT_CANCELLED — plain column |
| reference_id   | BIGINT       | plain column, **no FK**             |
| created_at     | TIMESTAMP    |                                     |
| sent_at        | TIMESTAMP    | nullable                            |

**No foreign keys on this table, deliberately.** An outbox row is a snapshot,
not a reference. Everything needed to send is copied in at queue time, so a
later email change or event delete can't corrupt the record of what was sent.

---

# PART 5 — THE DECISIONS THAT MATTER

These were reasoned through. Don't quietly change them; if you think one is
wrong, say so and why.

**Seat availability is derived, never stored.**
`available = seat_limit − COUNT(rsvp WHERE status='CONFIRMED')`. A stored
counter is a second source of truth that drifts on any missed decrement.
Watch the N+1 on list endpoints — one grouped `COUNT ... GROUP BY event_id`,
not a query per event.

**RSVP uses a pessimistic write lock on the event row.**
The core of the project. Two people clicking the last seat at once must not
both get confirmed.

```
1. SELECT ... FOR UPDATE on the event row
2. COUNT confirmed for that event
3. count < seat_limit ? CONFIRMED : WAITLISTED with position
4. insert rsvp
5. commit → lock released
```

The second user does **not** get an error — they get waitlisted. That's the
feature. 409 is only for a user who already has an RSVP on that event.

**Waitlist position is a stored column.** Renumbering on cancellation is safe
because it happens inside the same locked transaction.

**Email goes through the outbox table.** Queue rows in the _same transaction_ as
the business change, then a scheduled job sends them. Direct SMTP in the request
makes cancelling a 50-person event hang; a plain `@Async` thread loses mail on
restart. Outbox gives a fast response, restart-safety and free retries.

**Real-time is SSE, not WebSocket.** Data flows one way only — server tells
clients the seat count changed. Clients RSVP over normal POST. WebSocket would
be a bidirectional channel where only one direction is used. Publish a Spring
`ApplicationEvent` from the service layer; an SSE listener pushes it. The service
must know nothing about SSE. Heartbeat every 25s or load balancers drop idle
connections.

**Meeting link is a live permission check.** Returned in the API only to the
organiser and currently-confirmed attendees — disappears the instant an RSVP is
cancelled. Emailed in the reminder (24h before), not at RSVP time, so people who
cancelled early never receive it. Organisers can send early via a button, which
sets `link_sent_at`. If someone is promoted from the waitlist _after_ the link
was sent, their promotion email must include it.

**Scheduled jobs:** outbox sender (30s) · reminders (daily) · auto-complete past
events (daily). If ever run multi-instance these need ShedLock, or duplicate
emails go out.

---

# PART 6 — API

Base path `/v1`. Version prefix from day one.

### Auth

```
POST /v1/auth/register     public    {name,email,password,bio?}
POST /v1/auth/login        public    → {accessToken, user}
GET  /v1/auth/me           bearer
```

### Events

```
GET   /v1/events                    public     page,size,tag,mode,search
GET   /v1/events/{id}               public     meetingLink only if organiser or confirmed
POST  /v1/events                    bearer     caller becomes organiser
PATCH /v1/events/{id}               organiser  raising seatLimit auto-promotes
POST  /v1/events/{id}/cancel        organiser  queues emails to all
POST  /v1/events/{id}/complete      organiser
POST  /v1/events/{id}/send-link     organiser  sets link_sent_at
GET   /v1/events/my                 bearer
```

### RSVP

```
POST   /v1/events/{id}/rsvp         bearer     → CONFIRMED or WAITLISTED+position
DELETE /v1/events/{id}/rsvp         bearer     promotes next, queues their email
GET    /v1/events/{id}/attendees    organiser
GET    /v1/events/{id}/waitlist     organiser  ordered by position
GET    /v1/rsvps/my                 bearer
```

### Tags & stream

```
GET  /v1/tags                       public
POST /v1/tags                       bearer     trims+lowercases, returns existing if present
GET  /v1/events/{id}/stream         public     SSE seat updates
```

### Status codes

`200` ok · `201` created · `400` validation or invalid state transition ·
`401` no/bad token · `403` authenticated but not the organiser ·
`404` not found · `409` duplicate (email taken, already RSVPed)

Cancel and complete are POST actions rather than PATCH on a status field —
they're state transitions with side effects (emails, promotions), so explicit
endpoints are clearer to authorise and impossible to misuse via a generic update.

---

# PART 7 — WORKING AGREEMENT

## Commits

Conventional commits: `feat:`, `fix:`, `refactor:`, `chore:`, `test:`, `docs:`.
`refactor:` means behaviour is unchanged — if behaviour changed, it isn't a
refactor. One concern per commit.

## Definition of done

**Backend:** controller thin · all logic in the service · DTOs in and out, never
entities · typed exception with a message key · Bean Validation on the request
DTO · `@Transactional` on the right method · no magic values · unit test on the
service.

**Frontend:** endpoint in `ApiEndpoints` · query key in `QueryKeys` · hook in the
API file with its own invalidation · component calls the hook and nothing else ·
types imported from `types/`, not redeclared · no literals · loading and error
states handled · no prop drilling past two layers.

## How to work with me

- Explain _why_, not just _what_. The point of this project is learning.
- Flag it when I'm about to do something that will bite me later.
- Prefer boring, obvious code over clever code.
- Don't generate a whole feature in one shot — layer by layer, so I can follow.
- If something here is wrong or outdated, say so instead of working around it.
