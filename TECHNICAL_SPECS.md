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
12. [Bounded Context Reference Sheet](#12-bounded-context-reference-sheet)

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
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.5 |
| ORM | Spring Data JPA / Hibernate | (via Spring Boot parent) |
| Primary database | PostgreSQL + PostGIS | 15 / 3.x |
| Cache & real-time | Redis | 7.x |
| Schema migrations | Flyway | (via Spring Boot parent) |
| Validation | Spring Boot Validation (Bean Validation 3) | (via Spring Boot parent) |
| Testing | JUnit 5 + Testcontainers | latest |
| BDD | Cucumber (to be wired) | latest |
| Containerisation | Docker + Docker Compose | latest |

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
        └── db/migration/
            └── V1__init.sql

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
│   └── handler/           # CommandHandlers and QueryHandlers (use cases)
├── domain/
│   ├── entity/            # Aggregates and child entities (pure Java, no Spring/JPA annotations)
│   ├── valueobject/       # Immutable value objects specific to this module
│   ├── event/             # Domain events emitted by this module
│   ├── exception/         # Domain exceptions
│   └── repository/        # Repository interfaces (no implementation here)
└── infrastructure/
    └── persistence/       # JPA entities, Spring Data repositories, repository implementations
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
      test: ["CMD-SHELL", "pg_isready -U app -d dddriveby"]
      interval: 5s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 10

  app:
    build: .
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

### Migrations

All schemas and tables are created by Flyway. Migration scripts live in `src/main/resources/db/migration/` and follow the naming convention `V{version}__{description}.sql`.

The initial migration (`V1__init.sql`) creates the PostGIS extension, all schemas, all tables, and all indexes.

Flyway is **disabled** in the test profile — tests use `spring.jpa.hibernate.ddl-auto: create-drop` with H2 in-memory.

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
      ddl-auto: validate       # schema managed by Flyway — never create/update in prod
  flyway:
    enabled: true
    baseline-on-migrate: true
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

### Routing & Maps (Google Maps mock)

```
geolocation/infrastructure/adapters/MockRoutingAdapter.java
```
- `calculateRoute(origin, destination)` → distance from Haversine formula, fixed speed assumption
- `getEta(driverPosition, pickupPoint)` → fixed 5-minute ETA

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

## 12. Bounded Context Reference Sheet

### ride-management ⭐ Core Domain

| | |
|---|---|
| **Owns** | Ride, RideRequest, RideOffer, RideStatus state machine |
| **Exposes** | `createRideRequest`, `createRideOffer`, `getRideById`, `getRidesByPassenger` |
| **Emits** | `RideRequestedEvent`, `RideOfferedEvent`, `RideAcceptedEvent`, `RidePickedUpEvent`, `RideInProgressEvent`, `RideArrivedEvent`, `RideFinalizedEvent`, `RideCancelledEvent`, `RideIncidentEvent` |
| **Listens to** | `PaymentProcessedEvent` (to finalize), `MatchFoundEvent` (to move to Proposed) |
| **Schema** | `ride` |

---

### matching

| | |
|---|---|
| **Owns** | Match, Grouping, MatchingRules |
| **Exposes** | `getMatchForRide` |
| **Emits** | `MatchFoundEvent`, `MatchFailedEvent`, `GroupingCreatedEvent` |
| **Listens to** | `RideRequestedEvent`, `RideOfferedEvent` |
| **Depends on (via facade)** | `user-management`, `geolocation`, `territorial-configuration` |
| **Schema** | `matching` |

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

### geolocation

| | |
|---|---|
| **Owns** | RealTimePosition, Route, ETA |
| **Exposes** | `calculateRoute`, `getEta`, `updateDriverPosition`, `getDriversWithinRadius` |
| **Emits** | `DriverPositionUpdatedEvent` |
| **Listens to** | `DriverAvailabilityChangedEvent` |
| **External mocks** | `MockRoutingAdapter` (Google Maps) |
| **Note** | Uses Redis GEO commands for real-time position. PostGIS for persistent route data. |
| **Schema** | `geo` |

---

> *Last updated: 2026-04-29*
> *Any change to this document must be discussed with the team — it affects all bounded contexts.*
