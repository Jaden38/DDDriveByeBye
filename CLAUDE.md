# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Authoritative reference

`documentation/TECHNICAL_SPECS.md` is the single source of truth for the stack, layering, module contracts, schema-per-module rules, Redis usage, mock adapters, and the bounded-context reference sheet. **Read it before making non-trivial changes** — every rule below is summarized from it.

`documentation/UBIQUITOUS_LANGUAGE.md` defines domain terms (User, Driver, Ride Offer vs Ride Request, Immediate vs Scheduled Ride, Working Zone vs Activity Zone, etc.). Use these terms verbatim in code.

`features/*.feature` are Gherkin scenarios that describe target behavior per bounded context. They are the behavioral spec for modules not yet implemented.

`documentation/Steps.md` tracks implementation progress. As of now: **user-management** and **territorial-configuration** are implemented (tests deferred); **geolocation, ride-management, matching, pricing, payment, reputation, notification** are not yet implemented.

## Commands

There is no Maven wrapper — use the system `mvn` (Java 21 required).

```bash
# Build (skips tests)
mvn -B -DskipTests package

# Run all tests (uses H2 in-memory + Testcontainers for integration tests)
mvn test

# Run a single test class
mvn test -Dtest=TerritoryTest

# Run a single test method
mvn test -Dtest=TerritoryTest#shouldThrowWhenDeactivatingAlreadyInactiveTerritory

# Start infra only (Postgres + Redis) for local dev with hot reload
docker compose up -d postgres redis
mvn spring-boot:run

# Full stack via Docker
docker compose up --build
```

The app listens on `:8080`. Postgres exposes `:5432` (db `dddriveby`, user/password `app`/`app`), Redis on `:6379`.

## Architecture

**Modular monolith.** One Spring Boot deployable; bounded contexts isolated by package and enforced via the facade pattern.

### Per-module layout (every bounded context follows this)

```
com.dddrivebye.<module>/
├── <Module>ModuleConfig.java   # @Configuration + @ComponentScan boundary marker
├── api/                         # PUBLIC — only entry point for other modules
│   ├── <Module>Facade.java     # @Component delegating to handlers
│   ├── <Module>Controller.java # REST adapter, delegates to facade
│   └── dto/                    # DTOs returned by the facade
├── application/
│   ├── command/                # write-intent records
│   ├── query/                  # read-intent records
│   └── handler/                # @Service CommandHandlers / QueryHandlers
├── domain/
│   ├── entity/                 # aggregates — plain Java, NO Spring/JPA annotations
│   ├── valueobject/            # final, immutable, self-validating, static factories
│   ├── event/                  # extends shared.domain.event.BaseDomainEvent
│   ├── exception/
│   └── repository/             # interfaces only
└── infrastructure/
    └── persistence/            # JpaEntity, SpringDataRepository, Repository impl
```

### Inter-module rules (enforced by convention — break them and the architecture rots)

- A module may import **only** from `com.dddrivebye.<other-module>.api`. Never from another module's `domain/`, `application/`, or `infrastructure/`.
- Synchronous cross-module calls inject the other module's facade.
- Asynchronous reactions use Spring `@EventListener` on domain events.
- `shared/` kernel holds cross-cutting primitives only (`Money`, `GeoCoordinates`, `DateRange`, `BaseDomainEvent`, `DomainEventPublisher`) — no business logic.
- Allowed dependency directions are tabulated in `TECHNICAL_SPECS.md` §7. `ride-management` is core and has **no upstream deps**.

### Domain layer rules

- Aggregates have **private constructors**. Use `create(...)` for new instances and `reconstitute(...)` when rehydrating from persistence.
- All state mutations go through explicit methods that enforce invariants and throw domain exceptions on violation.
- Aggregates collect events in an internal list. The application handler calls `aggregate.pullDomainEvents()` after `repository.save(...)` and forwards each event to `DomainEventPublisher` (implemented by `SpringDomainEventPublisher`, backed by Spring's `ApplicationEventPublisher`).
- Value objects: `final` class, all fields `final`, private constructor, static factory (`of(...)`), value-based `equals`/`hashCode`.

### Persistence

- **One PostgreSQL schema per module** (`users`, `territory`, `ride`, `pricing`, `matching`, `geo`, `payment`, `notification`, `reputation`). Cross-schema SQL joins are forbidden — use facades.
- JPA entities live in `infrastructure/persistence/` and are **distinct classes** from domain entities. The repository implementation handles the mapping.
- Flyway migrations: `src/main/resources/db/migration/V{n}__{desc}.sql`. Hibernate is set to `validate` in prod — never let it auto-create or update.
- Production uses Postgres + PostGIS. Tests use H2 in PostgreSQL compatibility mode with `create-drop` and Flyway disabled (`src/test/resources/application.yml`). Redis auto-config is excluded from the test profile so tests don't need a running Redis.

### Redis

Used for real-time state only (driver positions via `GEOADD`/`GEORADIUS`, ride status cache). Key patterns and TTLs are in `TECHNICAL_SPECS.md` §9.

### External services

All external integrations (Stripe, Google Maps, FCM, Resend, Twilio) are mocked via `infrastructure/adapters/Mock*Adapter` classes. Real implementations should be drop-in replacements behind the same interface.

## Testing strategy

- **Domain**: pure JUnit 5 + Mockito. Target: 100% coverage on entities and value objects.
- **Application handlers**: unit tests with mocked repositories. Target: >80%.
- **Integration**: `@SpringBootTest` + `@Testcontainers` with `org.testcontainers:postgresql` (already on the classpath). Use mock adapters for external services.
- **BDD**: Cucumber bindings against the `.feature` files (wiring not yet in place — see `Steps.md`).
