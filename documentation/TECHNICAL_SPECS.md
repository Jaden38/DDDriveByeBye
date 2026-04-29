# Technical Specifications — VTC & Carpooling Mobility Platform

> This document is the single source of truth for the technical stack, conventions, and module contracts.
> Every developer (or AI assistant) implementing a bounded context must follow these specs exactly.

---

## Table of Contents

1. [Architecture](#1-architecture)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Docker Compose](#4-docker-compose)
5. [Spring Boot Module Conventions](#5-spring-boot-module-conventions)
6. [DDD Layer Conventions](#6-ddd-layer-conventions)
7. [Inter-Module Communication Rules](#7-inter-module-communication-rules)
8. [Database Conventions](#8-database-conventions)
9. [Redis Conventions](#9-redis-conventions)
10. [Mock External Services](#10-mock-external-services)
11. [Testing Conventions](#11-testing-conventions)
12. [Async Pipeline (BullMQ Equivalent)](#12-async-pipeline-bullmq-equivalent)
13. [Bounded Context Reference Sheet](#13-bounded-context-reference-sheet)

---

## 1. Architecture

**Modular Monolith** — single deployable Spring Boot application, hard boundaries between modules enforced by package structure and facade access rules.

Key rules:
- Each bounded context lives under its own top-level package (e.g., `com.dddrivebye.usermanagement`)
- Modules communicate **only** through their `api/` package — never by importing from another module's `domain/`, `application/`, or `infrastructure/`
- The `shared/` kernel contains only cross-cutting primitives — no business logic
- The database is shared at the infrastructure level but **each module owns its own PostgreSQL schema**

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 (build also runs on 23/24 — see Lombok note below) |
| Framework | Spring Boot | 3.3.5 |
| ORM | Spring Data JPA / Hibernate | (via Spring Boot parent) |
| Primary database | PostgreSQL + PostGIS | 15 / 3.x |
| Cache & real-time | Redis | 7.x |
| Schema migrations | Flyway | (via Spring Boot parent) |
| Validation | Spring Boot Validation (Bean Validation 3) | (via Spring Boot parent) |
| Boilerplate reduction | Lombok | 1.18.38 |
| Async / scheduling | Spring `@EnableAsync` + `TaskScheduler` | (via Spring Boot parent) |
| Testing | JUnit 5 + Mockito + AssertJ + Testcontainers | latest |
| BDD | Cucumber (to be wired) | latest |
| Containerisation | Docker + Docker Compose | latest |

**Lombok wiring.** Since JDK 23 the compiler default is `-proc:none`, which silently disables annotation processors. The build wires Lombok explicitly through `<annotationProcessorPaths>` on `maven-compiler-plugin` and excludes it from the Spring Boot fat jar. Use Lombok sparingly — only for boilerplate reduction on JPA entities, request/response payloads, and final-fields-only services. Domain entities, value objects, and aggregates stay hand-written so DDD invariants live in plain Java.

**Mockito + JDK 21+.** Mockito's default inline mock-maker can't redefine classes on JDK 21+ unless the byte-buddy agent is attached. The project ships `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` set to `mock-maker-subclass`, which works without the agent. Trade-off: cannot mock final classes / static methods. Override per-test via `Mockito.mock(..., withSettings().mockMaker(...))` if you need that.

---

## 3. Project Structure

```
src/
└── main/
    └── java/
        └── com/dddrivebye/
            ├── DddrivebyeApplication.java       # Spring Boot entry point
            ├── shared/
            │   ├── domain/
            │   │   ├── event/                   # BaseDomainEvent, DomainEventPublisher
            │   │   └── valueobject/             # Money, GeoCoordinates, DateRange
            │   └── infrastructure/
            │       └── event/                   # SpringDomainEventPublisher
            ├── usermanagement/
            │   ├── UserManagementModuleConfig.java   # @Configuration boundary marker
            │   ├── api/                              # PUBLIC — other modules may import from here only
            │   │   ├── UserManagementFacade.java
            │   │   ├── UserController.java
            │   │   └── dto/
            │   ├── application/
            │   │   ├── command/
            │   │   ├── query/
            │   │   └── handler/
            │   ├── domain/
            │   │   ├── entity/
            │   │   ├── valueobject/
            │   │   ├── event/
            │   │   ├── exception/
            │   │   └── repository/
            │   └── infrastructure/
            │       └── persistence/
            └── territorialconfiguration/
                ├── TerritorialConfigurationModuleConfig.java
                ├── api/                              # PUBLIC
            │   ├── TerritorialConfigurationFacade.java
            │   ├── TerritoryController.java
            │   └── dto/
                ├── application/
            │   ├── command/
            │   ├── query/
            │   └── handler/
                ├── domain/
            │   ├── entity/
            │   ├── valueobject/
            │   ├── exception/
            │   └── repository/
                └── infrastructure/
                    └── persistence/

src/
└── main/
    └── resources/
        ├── application.yml
        └── schema.sql                # CREATE SCHEMA IF NOT EXISTS for each module

src/
└── test/
    └── resources/
        └── application.yml           # H2 in-memory, Flyway disabled, Redis excluded

features/                             # Gherkin BDD scenarios (source of truth for behaviour)
documentation/
docker-compose.yml
Dockerfile
pom.xml
```

### Internal structure of every module

```
<module-name>/
├── api/                    # PUBLIC — the module's boundary. Other modules import ONLY from here.
│   ├── <Module>Facade.java # Single entry point for commands and queries
│   ├── <Module>Controller.java (optional — HTTP adapter)
│   └── dto/               # DTOs exposed by the facade
├── application/
│   ├── command/           # Command records (write intent)
│   ├── query/             # Query records (read intent)
│   ├── handler/           # CommandHandlers and QueryHandlers (use cases)
│   ├── port/              # (optional) Interfaces describing what the module needs from
│   │                      #   not-yet-implemented modules — paired with stub adapters
│   │                      #   under infrastructure/adapters/. See matching's GeolocationPort.
│   └── async/             # (optional) @Async @EventListener consumers and TaskScheduler
│                          #   bindings — the module's "queue worker" entry points.
│                          #   See §12 for the project-wide async pipeline.
├── domain/
│   ├── entity/            # Aggregates and child entities (pure Java, no Spring/JPA annotations)
│   ├── valueobject/       # Immutable value objects specific to this module
│   ├── event/             # Domain events emitted by this module
│   ├── exception/         # Domain exceptions
│   ├── repository/        # Repository interfaces (no implementation here)
│   └── service/           # (optional) Pure-Java domain services — multi-aggregate
│                          #   logic that doesn't belong on a single entity.
│                          #   See matching's DriverRanking.
└── infrastructure/
    ├── persistence/       # JPA entities, Spring Data repositories, repository implementations
    └── adapters/          # (optional) Adapters for external services or stub adapters
                           #   for not-yet-implemented sibling modules.
```

---

## 4. Docker Compose

Three services are defined in `docker-compose.yml`:

```yaml
services:

  postgres:
    image: postgis/postgis:15-3.4
    environment:
      POSTGRES_DB: dddriveby
      POSTGRES_USER: app
      POSTGRES_PASSWORD: app
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U app -d dddriveby" ]
      interval: 5s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: [ "CMD", "redis-cli", "ping" ]
      interval: 5s
      timeout: 3s
      retries: 10

  app:
    build: ../../../../../Users/damie/Downloads
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/dddriveby
      SPRING_DATASOURCE_USERNAME: app
      SPRING_DATASOURCE_PASSWORD: app
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

Run locally (without Docker for the app, for hot reload):

```bash
docker compose up -d postgres redis   # start infra only
./mvnw spring-boot:run                # run app on host
```

The Dockerfile uses a two-stage Maven build and produces a self-contained JAR targeting Java 21.

---

## 5. Spring Boot Module Conventions

Each bounded context has a `@Configuration` class at the root of its package that acts as a boundary marker and triggers component scanning:

```java
// com.dddrivebye.usermanagement.UserManagementModuleConfig.java
@Configuration
@ComponentScan
public class UserManagementModuleConfig {
}
```

The Javadoc on this class **must** state: "Other modules MUST depend only on classes under `com.dddrivebye.<module>.api`."

### Facade pattern

Each module exposes a `@Component` facade in its `api/` package. The facade delegates to internal command and query handlers. Other modules may only inject the facade — never a handler, repository, or domain type directly.

```java
@Component
public class UserManagementFacade {

    private final UserCommandHandler commandHandler;
    private final UserQueryHandler queryHandler;

    // All public methods are in terms of commands/queries and DTOs from api/dto/
    public UUID registerIndividualAccount(RegisterIndividualAccountCommand command) {
        return commandHandler.handle(command);
    }

    public Optional<UserDto> getUserById(UUID userId) {
        return queryHandler.handle(new GetUserByIdQuery(userId));
    }
}
```

### REST Controllers

HTTP controllers live in `api/` and delegate exclusively to the module's facade:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementFacade facade;

    @PostMapping("/individual")
    public ResponseEntity<Void> registerIndividual(@Valid @RequestBody IndividualRegistrationRequest req) {
        UUID id = facade.registerIndividualAccount(
            new RegisterIndividualAccountCommand(req.fullName(), req.email(), req.phoneNumber()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }
}
```

Request/response records are defined as inner records of the controller class and are never shared across modules.

---

## 6. DDD Layer Conventions

### Entities & Aggregates

```java
// domain/entity/Territory.java
public class Territory {

    private final TerritoryId id;
    private String name;
    private GeographicZone zone;
    private TerritorialRule rule;
    private boolean active;

    // Private constructor — use static factory methods only
    private Territory(TerritoryId id, String name, GeographicZone zone,
                      TerritorialRule rule, Set<RegulatoryConstraint> constraints, boolean active) { ... }

    // Factory for new aggregates
    public static Territory create(String name, GeographicZone zone, TerritorialRule rule) { ... }

    // Factory for rehydration from persistence
    public static Territory reconstitute(TerritoryId id, String name, GeographicZone zone,
                                         TerritorialRule rule, Set<RegulatoryConstraint> constraints,
                                         boolean active) { ... }

    // State changes through explicit methods — never by setting fields directly
    public void deactivate() {
        if (!active) throw new InvalidTerritoryOperationException("Territory already inactive: " + name);
        active = false;
    }
}
```

**Rules:**
- Domain entities are **plain Java classes** — no Spring, no JPA annotations
- Constructors are private; use `create(...)` for new aggregates and `reconstitute(...)` for rehydration
- All state changes go through explicit methods that enforce invariants
- State violation throws a domain exception

### Value Objects

```java
// shared/domain/valueobject/Money.java
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) { ... }

    public static Money of(BigDecimal amount, String currencyCode) {
        if (amount.signum() < 0) throw new IllegalArgumentException(...);
        return new Money(amount.setScale(...), Currency.getInstance(currencyCode));
    }

    public Money add(Money other) { ensureSameCurrency(other); return new Money(this.amount.add(other.amount), currency); }
    // equals(), hashCode() implemented based on value
}
```

**Rules:**
- `final` class, all fields `final` — immutable
- Private constructor + static factory (`of(...)`)
- `equals()` and `hashCode()` implemented by value
- Self-validating (throws on construction if invariants are violated)

### Domain Events

```java
// shared/domain/event/BaseDomainEvent.java
public abstract class BaseDomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    // getters only
}

// usermanagement/domain/event/DriverAvailabilityChangedEvent.java
public final class DriverAvailabilityChangedEvent extends BaseDomainEvent {
    private final UserId userId;
    private final AvailabilityStatus previousStatus;
    private final AvailabilityStatus newStatus;
    // ...
}
```

Events are collected inside the aggregate and drained by the application handler after persistence:

```java
// In User aggregate
private final List<BaseDomainEvent> domainEvents = new ArrayList<>();

public List<BaseDomainEvent> pullDomainEvents() {
    List<BaseDomainEvent> snapshot = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(snapshot);
}
```

```java
// In UserCommandHandler
private void publishEvents(User user) {
    List<BaseDomainEvent> events = user.pullDomainEvents();
    events.forEach(eventPublisher::publish);
}
```

Domain events are published via `SpringDomainEventPublisher`, which delegates to Spring's `ApplicationEventPublisher`.

### Repository Interfaces

```java
// domain/repository/TerritoryRepository.java
public interface TerritoryRepository {
    void save(Territory territory);
    Optional<Territory> findById(TerritoryId id);
    Optional<Territory> findByName(String name);
    List<Territory> findActiveCovering(GeoCoordinates point);
}
```

**Rule:** The interface lives in `domain/repository/` — the implementation lives in `infrastructure/persistence/`.

### Application Handlers

Commands and queries are plain Java records:

```java
// application/command/CreateTerritoryCommand.java
public record CreateTerritoryCommand(
    String name,
    double centerLatitude,
    double centerLongitude,
    double radiusKm,
    BigDecimal perKilometerRate,
    // ...
    List<RegulatoryConstraintInput> constraints
) {
    public record RegulatoryConstraintInput(String code, String description) {}
}
```

Handlers are `@Service` beans:

```java
@Service
public class TerritoryCommandHandler {

    private final TerritoryRepository territories;

    @Transactional
    public UUID handle(CreateTerritoryCommand command) {
        // 1. Build domain objects from command primitives
        // 2. Call aggregate factory
        // 3. Persist via repository
        // 4. Return identity
        Territory territory = Territory.create(command.name(), zone, rule);
        territories.save(territory);
        return territory.id().value();
    }
}
```

---

## 7. Inter-Module Communication Rules

### Synchronous calls (same JVM)

When module A needs data from module B synchronously, it injects B's facade:

```java
// pricing module depending on territorial-configuration
@Service
public class FareCalculationService {
    private final TerritorialConfigurationFacade territorialConfig;

    public Money calculateFare(...) {
        Optional<TerritoryDto> territory = territorialConfig.getTerritoryForCoordinates(lat, lon);
        // ...
    }
}
```

Other modules **must not** import from `domain/`, `application/`, or `infrastructure/` of another module.

### Asynchronous communication (domain events)

When a module needs to react to something that happened in another module, it listens to the published domain event via Spring's `@EventListener`:

```java
// notification module reacting to a ride event
@Component
public class OnDriverAvailabilityChangedListener {

    @EventListener
    public void handle(DriverAvailabilityChangedEvent event) {
        // react asynchronously
    }
}
```

**Domain-event classes are the only sanctioned exception** to the "no imports from another module's domain" rule: the listener must reference the published event type. Treat the event class — its name, package path, fields, and emission contract — as part of the **public API** of the producing module. Adding fields is fine; renaming or removing them is a breaking change. Place all events under `domain/event/` so they're easy to enumerate.

For *delayed* or *retry* jobs (e.g. matching's 30-second proposal expiry), use Spring's `TaskScheduler` from inside an `@EventListener`. See §12.

### Allowed dependency directions (from context map)

| Module | May call facade of |
|---|---|
| `ride-management` | `pricing`, `user-management`, `notification`, `payment` |
| `matching` | `user-management`, `geolocation`, `territorial-configuration` |
| `pricing` | `geolocation`, `territorial-configuration` |
| `reputation` | `user-management` |
| Any module | `notification` (via event only) |

**Forbidden:** any module importing from `ride-management` domain — it is the core and has no upstream dependencies.

---

## 8. Database Conventions

### One schema per module

| Module | Schema |
|---|---|
| `ride-management` | `ride` |
| `matching` | `matching` |
| `pricing` | `pricing` |
| `reputation` | `reputation` |
| `user-management` | `users` |
| `payment` | `payment` |
| `notification` | `notification` |
| `territorial-configuration` | `territory` |
| `geolocation` | `geo` |

### JPA entities

JPA entities live in `infrastructure/persistence/` and carry all JPA annotations. They are **never** the same class as domain entities.

```java
@Entity
@Table(schema = "territory", name = "territories")
public class TerritoryJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // ... columns and relationships
}
```

Spring Data JPA interfaces also live in `infrastructure/persistence/`:

```java
public interface SpringDataTerritoryRepository extends JpaRepository<TerritoryJpaEntity, UUID> {
    Optional<TerritoryJpaEntity> findByName(String name);

    @Query("select t from TerritoryJpaEntity t where t.active = true")
    List<TerritoryJpaEntity> findAllActive();
}
```

The domain repository interface is implemented by a `@Repository` class that uses the Spring Data interface as a delegate and handles all mapping between JPA entities and domain objects.

### Schema management

For the current development phase the schema is managed by **Hibernate's `ddl-auto: create-drop`**, not Flyway. Each app start drops and recreates all tables from the JPA `@Entity` classes; volumes are wiped between runs (`docker compose down -v`). Flyway is disabled (`spring.flyway.enabled: false`).

The **only** SQL file in `src/main/resources/` is `schema.sql`, which Spring runs at datasource init (before Hibernate generates tables). Its sole job is to create the per-module PG schemas, since Hibernate does not auto-create namespaces:

```sql
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS territory;
CREATE SCHEMA IF NOT EXISTS geo;
CREATE SCHEMA IF NOT EXISTS ride;
CREATE SCHEMA IF NOT EXISTS matching;
```

**Adding a new module's schema:** add one line to `schema.sql` and a JPA entity with `@Table(schema = "<module>")`. No migration to write, no version number to claim.

**PostGIS:** the `postgis/postgis:15-3.4` image creates the extension automatically in the default DB.

**Trade-offs.** This is fast for development but loses data on every restart and offers no schema-evolution path. When the project needs durable schemas across deploys (production, shared staging, multi-instance), reintroduce Flyway: turn `ddl-auto: validate`, set `spring.flyway.enabled: true`, and add a baseline migration that snapshots the Hibernate-generated schema. At that point a version registry becomes useful again.

### Cross-schema joins are forbidden

Modules **never** do SQL joins across schemas. If module A needs data from module B, it calls B's facade.

### Production configuration (`application.yml`)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/dddriveby}
    username: ${SPRING_DATASOURCE_USERNAME:app}
    password: ${SPRING_DATASOURCE_PASSWORD:app}
  jpa:
    hibernate:
      ddl-auto: create-drop    # development: tables are dropped + recreated each boot
  flyway:
    enabled: false
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql   # creates PG schemas before Hibernate runs
```

### Test configuration (`src/test/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:dddrivebye;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  flyway:
    enabled: false
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
```

---

## 9. Redis Conventions

Redis is available as a Spring Data Redis dependency. It is used for:

| Use case | Key pattern | TTL |
|---|---|---|
| Driver real-time position | `geo:driver:{driverId}:position` | 30s |
| Available drivers index | `geo:available-drivers` (Redis GEO set) | — |
| Ride status cache | `ride:{rideId}:status` | 60s |

Use Redis GEO commands (`GEOADD`, `GEORADIUS`) for proximity queries in the Geolocation and Matching modules.

Redis is **excluded from the test profile** via `spring.autoconfigure.exclude` to avoid requiring a running Redis instance during unit and integration tests.

---

## 10. Mock External Services

All external services are mocked. Mocks live in each module's `infrastructure/adapters/` folder and implement the same interface as the real service would. The real implementation can be swapped without touching the domain or application layers.

### Payment (Stripe mock)

```
payment/infrastructure/adapters/MockPaymentAdapter.java
```
- `charge(amount, currency, customerId)` → always returns a successful mock transaction
- `refund(transactionId, amount)` → always returns success

### Routing & Maps (Google Maps mock) ✅ Implemented

```
geolocation/infrastructure/adapter/MockRoutingAdapter.java
```
- `calculateRoute(origin, destination)` → distance from Haversine formula, 30 km/h average duration
- `getEta(driverPosition, pickupPoint)` → fixed 5-minute ETA

### Stub adapters for not-yet-implemented sibling modules

When module A needs module B's facade but B isn't built yet, A defines a **port** under `application/port/` and ships a **stub adapter** under `infrastructure/adapters/`. The stub returns the most permissive empty / neutral result so A can boot end-to-end. Switch to the real implementation behind a Spring profile.

| Module | Port | Stub adapter | Real-implementation profile |
|---|---|---|---|
| matching | `GeolocationPort` | `StubGeolocationAdapter` | `geolocation-real` |
| matching | `ReputationPort` | `StubReputationAdapter` | `reputation-real` |
| matching | `RideOfferCatalogPort` | `StubRideOfferCatalogAdapter` | `ride-management-real` |

### Push Notifications (FCM mock)

```
notification/infrastructure/adapters/MockPushAdapter.java
```
- `send(deviceToken, title, body)` → logs: `[MOCK PUSH] → {deviceToken}: {title}`

### Email (Resend mock)

```
notification/infrastructure/adapters/MockEmailAdapter.java
```
- `send(to, subject, html)` → logs: `[MOCK EMAIL] → {to}: {subject}`

### SMS (Twilio mock)

```
notification/infrastructure/adapters/MockSmsAdapter.java
```
- `send(phoneNumber, message)` → logs: `[MOCK SMS] → {phoneNumber}: {message}`

---

## 11. Testing Conventions

### Unit tests

- Test domain logic in complete isolation — no Spring context, no database, no HTTP
- Use plain JUnit 5 + Mockito
- Naming: `<Subject>Test.java`

```java
class TerritoryTest {

    @Test
    void shouldThrowWhenDeactivatingAlreadyInactiveTerritory() {
        Territory territory = Territory.create("Lyon", zone, rule);
        territory.deactivate();

        assertThatThrownBy(territory::deactivate)
            .isInstanceOf(InvalidTerritoryOperationException.class);
    }
}
```

### Integration tests

- Spin up PostgreSQL via Testcontainers (`org.testcontainers:postgresql`)
- Annotate with `@Testcontainers` and `@SpringBootTest`
- Redis is excluded via the test application profile
- Use mock adapters for external services

### E2E / BDD tests

- Driven by `.feature` files in `features/`
- Use **Cucumber** with step definitions mapping to HTTP calls against the running application
- All external services use mock adapters

```
features/territorial_configuration.feature  →  tests bound to step definitions
```

### Coverage targets

| Layer | Target |
|---|---|
| Domain (entities, value objects) | 100% |
| Application (handlers) | > 80% |
| Infrastructure (adapters, persistence) | integration tests only |

---

## 12. Async Pipeline (BullMQ Equivalent)

The original system design called out BullMQ producers in ride-management and a BullMQ consumer in matching. BullMQ is Node-only; the Java/Spring equivalent in this codebase is:

- **Spring `@Async` + `ApplicationEventPublisher`** as the queue itself — a producer publishes a domain event, the consumer's `@Async @EventListener` runs on a dedicated thread pool. No external broker.
- **Spring `TaskScheduler`** as the delayed-job mechanism — for one-shot timers like the 30-second proposal expiry.

### Where it lives

Each module that consumes async jobs adds an `application/async/` package containing:
- A `<Module>AsyncConfig` with `@EnableAsync` + `@EnableScheduling` and dedicated `ThreadPoolTaskExecutor` / `ThreadPoolTaskScheduler` beans (named `<module>TaskExecutor` / `<module>TaskScheduler`).
- Listener `@Component`s — `@Async("<module>TaskExecutor") @EventListener` for instant work, plain `@EventListener` that schedule via `TaskScheduler` for delayed work.

### Reference implementation (matching)

| Listener | Trigger | Action |
|---|---|---|
| `MatchingTriggerListener` | `RideRequestedEvent` | Resolves territory via `TerritorialConfigurationFacade`, runs `MatchingFacade.runImmediateMatching(...)` on `matchingTaskExecutor` |
| `ProposalExpiryScheduler` | `MatchProposalSentEvent` | Schedules `MatchingFacade.expireProposal(rideId)` for `event.expiresAt()` via `matchingTaskScheduler` |

Pool sizes (matching, as of today): executor 4–16 threads + 200 queue, scheduler 2 threads. Tune per module based on observed load.

### What this trades against BullMQ proper

- **In-JVM only.** A scheduled timer dies on app crash / restart. Acceptable for a single-instance modular monolith; not acceptable once you scale to multiple replicas or need durable retries.
- **No backpressure across instances.** All work runs in the publishing JVM.
- **No dead-letter queue, no replay, no visibility.** Failures are caught and logged in the listener; ops has no inbox.

### Migration path

When durability or multi-instance dispatch becomes a requirement, swap the trigger source — keep the listener's body, replace the `@EventListener` with a Redis Streams consumer (or Kafka, RabbitMQ). Producers continue to publish the same domain event types via `ApplicationEventPublisher`; an outbox-style relay forwards them to the queue. Domain logic is unaffected.

### Listener contract checklist

Every async listener:
1. Catches `RuntimeException` and logs explicitly. `@Async` swallows uncaught exceptions; without explicit logging, failed jobs vanish.
2. Is idempotent or guards against double-fire — Spring's local event bus guarantees at-least-once within the JVM but offers no across-restart deduplication.
3. Reads its inputs from the event payload and from facades — never from another module's repository.

---

## 13. Bounded Context Reference Sheet

### ride-management ⭐ Core Domain — ⚠️ Partial

| | |
|---|---|
| **Owns** | Ride aggregate (State pattern: Requested, Proposed, Accepted, PickedUp, InProgress, Arrived, Finalized, Cancelled, Incident), RideRequest, RideOffer |
| **Exposes** | `RideManagementFacade` — currently `requestRide`, `getRideById` |
| **Target surface** (from original design) | `createRideRequest`, `createRideOffer`, `getRideById`, `getRidesByPassenger` |
| **Emits** | `RideRequestedEvent` (on `Ride.create(...)`), `RideAcceptedEvent` (declared, **not yet emitted from any state transition**), `RideFinalizedEvent` (currently emitted from `arrive()` — to be moved to a true finalize step once payment listens) |
| **Target events** (from original design, not yet implemented) | `RideOfferedEvent`, `RidePickedUpEvent`, `RideInProgressEvent`, `RideArrivedEvent`, `RideCancelledEvent`, `RideIncidentEvent` |
| **Listens to** | (target) `PaymentProcessedEvent` (to finalize), `MatchFoundEvent` (to move to Proposed) — not yet implemented |
| **Outstanding** | RideOffer / RideRequest aggregates exist but have no repository implementation and aren't reachable from the facade. Currently no unit tests beyond `RideTest` (3 tests covering `Ride.create` + `RideRequestedEvent` emission). |
| **REST** | `POST /api/rides`, `GET /api/rides/{id}` |
| **Schema** | `ride` (tables: `rides`, `ride_requests`, `ride_offers`) |

---

### matching ✅ Implemented (BDD bindings deferred)

| | |
|---|---|
| **Owns** | `Match` aggregate (state machine `SEARCHING → PROPOSED → ACCEPTED/UNMATCHED/CANCELLED`), `Grouping` aggregate (carpooling), `DriverRanking` domain service (zone → distance → reputation), `ProposalWindow` value object |
| **Exposes** | `MatchingFacade` — `runImmediateMatching`, `runScheduledMatching`, `acceptProposal`, `declineProposal`, `expireProposal`, `cancelMatch`, `evaluateGrouping`, `dissolveGroupingForRideRequest`, `getMatchForRide`, `searchRideOffers` |
| **Emits** | `MatchProposalSentEvent`, `MatchFoundEvent`, `MatchFailedEvent`, `GroupingCreatedEvent`, `GroupingDissolvedEvent` |
| **Listens to** | `RideRequestedEvent` (via `MatchingTriggerListener`, async on `matchingTaskExecutor`), `MatchProposalSentEvent` (via `ProposalExpiryScheduler`, schedules `expireProposal` at the deadline) |
| **Depends on (via facade)** | `user-management`, `territorial-configuration` |
| **Depends on (via port + stub adapter, until sibling lands)** | `geolocation` (`GeolocationPort` ↔ `StubGeolocationAdapter`), `reputation` (`ReputationPort` ↔ `StubReputationAdapter`), `ride-management` (`RideOfferCatalogPort` ↔ `StubRideOfferCatalogAdapter`) |
| **Async pipeline** | `MatchingAsyncConfig` provides `matchingTaskExecutor` (4–16 threads, queue 200) and `matchingTaskScheduler` (2 threads). See §12. |
| **REST** | `POST /api/matching/immediate`, `POST /api/matching/scheduled`, `POST /api/matching/{rideId}/{accept,decline,expire,cancel}`, `GET /api/matching/{rideId}`, `GET /api/matching/ride-offers/search` |
| **Schema** | `matching` (tables: `matches`, `match_exclusions`, `groupings`, `grouping_members`) |
| **Tests** | 90 unit tests (full domain coverage, handler coverage with mocked facades/ports, async listener coverage). Cucumber bindings against `features/matching.feature` deferred. |

---

### pricing

| | |
|---|---|
| **Owns** | FareEstimate, FinalPrice, DynamicPricingRules |
| **Exposes** | `calculateFareEstimate`, `calculateFinalPrice`, `calculateRideOfferPricePerSeat` |
| **Emits** | `FareCalculatedEvent` |
| **Listens to** | `RideRequestedEvent` (estimate), `RideArrivedEvent` (final price) |
| **Depends on (via facade)** | `geolocation`, `territorial-configuration` |
| **Schema** | `pricing` |

---

### reputation

| | |
|---|---|
| **Owns** | Rating, ReputationScore, Penalty, Restriction |
| **Exposes** | `getReputationScore`, `isEligible` |
| **Emits** | `ReputationUpdatedEvent`, `PenaltyAppliedEvent`, `RestrictionAppliedEvent`, `RestrictionLiftedEvent` |
| **Listens to** | `RideFinalizedEvent`, `RideCancelledEvent` |
| **Depends on (via facade)** | `user-management` |
| **Schema** | `reputation` |

---

### user-management ✅ Implemented

| | |
|---|---|
| **Owns** | User, DriverProfile, VehicleProfile, Availability, ActivityZone, WorkingZone |
| **Exposes** | `UserManagementFacade` — `getUserById`, `getDriverProfile`, `getAvailableDriversNear`, `isDriverAvailable`, `registerIndividualAccount`, `registerProfessionalAccount`, `addDriverProfile`, `approveDriverProfile`, `rejectDriverProfile`, `registerVehicle`, `activateAvailability`, `deactivateAvailability`, `defineActivityZone`, `defineWorkingZone` |
| **Emits** | `DriverAvailabilityChangedEvent`, `DriverProfileValidatedEvent`, `DriverProfileRejectedEvent`, `AccountRestrictedEvent` |
| **Listens to** | `RestrictionAppliedEvent`, `RestrictionLiftedEvent` |
| **REST** | `GET/POST /api/users`, `GET/POST /api/users/{id}/driver-profile`, `POST /api/users/{id}/vehicles`, `PUT /api/users/{id}/availability/activate|deactivate`, `PUT /api/users/{id}/activity-zone`, `PUT /api/users/{id}/working-zone`, `GET /api/users/drivers/available` |
| **Schema** | `users` (tables: `users`, `vehicles`, `vehicle_options`) |

---

### payment

| | |
|---|---|
| **Owns** | Payment, PaymentMethod, Transaction, Refund |
| **Exposes** | `processPayment`, `refund`, `getPaymentStatus` |
| **Emits** | `PaymentProcessedEvent`, `PaymentFailedEvent`, `RefundProcessedEvent` |
| **Listens to** | `RideArrivedEvent` |
| **External mocks** | `MockPaymentAdapter` (Stripe) |
| **Schema** | `payment` |

---

### notification

| | |
|---|---|
| **Owns** | Notification, NotificationChannel, NotificationPreference |
| **Exposes** | none (purely reactive) |
| **Emits** | none |
| **Listens to** | `RideAcceptedEvent`, `RideCancelledEvent`, `RideFinalizedEvent`, `MatchFoundEvent`, `PaymentFailedEvent`, `RestrictionAppliedEvent`, `RestrictionLiftedEvent` |
| **External mocks** | `MockPushAdapter` (FCM), `MockEmailAdapter` (Resend), `MockSmsAdapter` (Twilio) |
| **Schema** | `notification` |

---

### territorial-configuration ✅ Implemented

| | |
|---|---|
| **Owns** | Territory, TerritorialRule, RegulatoryConstraint, GeographicZone |
| **Exposes** | `TerritorialConfigurationFacade` — `createTerritory`, `updateTerritorialRules`, `deactivateTerritory`, `getRulesForTerritory`, `getTerritoryForCoordinates`, `isCoordinatesCovered` |
| **Emits** | none |
| **Listens to** | none |
| **REST** | `POST /api/territories`, `GET /api/territories/{id}`, `GET /api/territories/covering`, `GET /api/territories/covered`, `PUT /api/territories/{id}/rules`, `DELETE /api/territories/{id}` |
| **Schema** | `territory` (tables: `territories`, `territory_constraints`) |

---

### geolocation ✅ Implemented (BDD bindings deferred)

| | |
|---|---|
| **Owns** | `RealTimePosition` aggregate, `Route` aggregate, `Eta` value object |
| **Exposes** | `GeolocationFacade` — `updateDriverPosition`, `removeDriverPosition`, `getDriverPosition`, `getDriversWithinRadius`, `calculateRoute`, `getEta` |
| **Emits** | `DriverPositionUpdatedEvent` (declared) |
| **Listens to** | `DriverAvailabilityChangedEvent` — `DriverAvailabilityEventHandler` removes the driver from the Redis GEO set on `OFFLINE` |
| **External mocks** | `MockRoutingAdapter` (Google Maps) — Haversine distance + 30 km/h average duration + fixed 5-minute ETA |
| **Note** | Uses Redis GEO commands (`GEOADD`, `GEORADIUS`) for real-time position. PostGIS GIST index on `geo.routes` for persistent route data. |
| **REST** | `/api/geolocation` |
| **Schema** | `geo` (table: `routes`) |
| **Tests** | 39 unit tests (domain VOs, entities, MockRoutingAdapter, handlers). Cucumber bindings against `features/geolocation.feature` deferred. |

---

> *Last updated: 2026-04-29 — switched §8 to Hibernate-managed schemas (Flyway disabled, `schema.sql` bootstraps PG namespaces, `ddl-auto: create-drop`); added §12 async pipeline; refreshed module statuses (matching ✅, geolocation ✅, ride-management partial); Lombok / Mockito mock-maker conventions.*
> *Any change to this document must be discussed with the team — it affects all bounded contexts.*
