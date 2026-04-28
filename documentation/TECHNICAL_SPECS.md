# Technical Specifications — VTC & Carpooling Mobility Platform

> This document is the single source of truth for the technical stack, conventions, and module contracts.
> Every developer (or AI assistant) implementing a bounded context must follow these specs exactly.

---

## Table of Contents

1. [Architecture](#1-architecture)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Docker Compose](#4-docker-compose)
5. [NestJS Module Conventions](#5-nestjs-module-conventions)
6. [DDD Layer Conventions](#6-ddd-layer-conventions)
7. [Inter-Module Communication Rules](#7-inter-module-communication-rules)
8. [Database Conventions](#8-database-conventions)
9. [Redis & BullMQ Conventions](#9-redis--bullmq-conventions)
10. [Mock External Services](#10-mock-external-services)
11. [Testing Conventions](#11-testing-conventions)
12. [Bounded Context Reference Sheet](#12-bounded-context-reference-sheet)

---

## 1. Architecture

**Modular Monolith** — single deployable application, hard boundaries between modules.

Key rules:
- Each bounded context = one NestJS module
- Modules communicate **only** through their `interface/` folder — never by importing directly into another module's `domain/` or `application/`
- The `shared/` kernel contains only cross-cutting primitives — no business logic
- The database is shared at the infrastructure level but **each module owns its own PostgreSQL schema**

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | TypeScript | 5.x |
| Runtime | Node.js | 20.x LTS |
| Framework | NestJS | 10.x |
| ORM | TypeORM | 0.3.x |
| Primary database | PostgreSQL + PostGIS | 15 / 3.x |
| Cache & real-time | Redis | 7.x |
| Job queues | BullMQ | 5.x |
| Validation | class-validator + class-transformer | latest |
| Testing | Jest + Cucumber.js | latest |
| Containerisation | Docker + Docker Compose | latest |

---

## 3. Project Structure

```
src/
├── modules/
│   ├── ride-management/
│   ├── matching/
│   ├── pricing/
│   ├── reputation/
│   ├── user-management/
│   ├── payment/
│   ├── notification/
│   ├── territorial-configuration/
│   └── geolocation/
├── shared/
│   ├── domain/
│   │   ├── value-objects/    # Money, GeoCoordinates, DateRange, etc.
│   │   └── events/           # BaseDomainEvent, IEventBus
│   └── infrastructure/       # TypeORM base config, Redis client, logger
└── app/
    ├── config/               # environment variables, config service
    └── http/                 # root AppModule, global pipes, guards

features/                     # Gherkin BDD scenarios (source of truth for behaviour)
tests/
├── unit/
├── integration/
└── e2e/
documentation/
```

### Internal structure of every module

```
<module-name>/
├── domain/
│   ├── entities/             # Aggregates and entities (pure domain, no framework)
│   ├── value-objects/        # Immutable primitives specific to this module
│   ├── events/               # Domain events emitted by this module
│   ├── services/             # Domain services (stateless logic not belonging to an entity)
│   └── repositories/         # Repository interfaces (no implementation here)
├── application/
│   ├── commands/             # Command DTOs (write intent)
│   ├── queries/              # Query DTOs (read intent)
│   └── handlers/             # CommandHandlers and QueryHandlers (use cases)
├── infrastructure/
│   ├── persistence/          # TypeORM entities, repository implementations, migrations
│   └── adapters/             # Implementations of external service interfaces (mocks here)
└── interface/                # The module's public API — what other modules may import
```

---

## 4. Docker Compose

Three services are required locally:

```yaml
# docker-compose.yml (reference — actual file at repo root)

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

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    build: .
    depends_on:
      - postgres
      - redis
    environment:
      DATABASE_URL: postgresql://app:app@postgres:5432/dddriveby
      REDIS_URL: redis://redis:6379
    ports:
      - "3000:3000"

volumes:
  postgres_data:
```

Run locally:
```bash
docker compose up -d postgres redis   # start deps only
npm run start:dev                     # run app outside Docker for hot reload
```

---

## 5. NestJS Module Conventions

Every bounded context is declared as a NestJS `@Module`. The module file lives at the root of the bounded context folder.

```typescript
// src/modules/ride-management/ride-management.module.ts
@Module({
  imports: [TypeOrmModule.forFeature([...]), BullModule.registerQueue(...)],
  providers: [
    // Handlers
    CreateRideRequestHandler,
    // Repository bindings
    { provide: IRideRepository, useClass: TypeOrmRideRepository },
  ],
  exports: [RideManagementInterface], // only export the interface facade
})
export class RideManagementModule {}
```

**Rules:**
- Only one `exports` entry: the `interface/` facade class or service
- `providers` are internal — never export handlers, repositories, or domain services directly
- Import other modules only by their `interface/` export, never their internals

---

## 6. DDD Layer Conventions

### Entities & Aggregates

```typescript
// domain/entities/ride.entity.ts
export class Ride {
  private constructor(
    public readonly id: RideId,
    private status: RideStatus,
    // ...
  ) {}

  static create(props: CreateRideProps): Ride { ... }

  // State transitions — throw on invalid transition
  accept(): void {
    if (this.status !== RideStatus.Proposed) {
      throw new InvalidTransitionError(this.status, RideStatus.Accepted);
    }
    this.status = RideStatus.Accepted;
    this.addDomainEvent(new RideAcceptedEvent(this.id));
  }
}
```

**Rules:**
- Entities are plain TypeScript classes — **no NestJS decorators, no TypeORM decorators**
- TypeORM entities live in `infrastructure/persistence/` as separate classes
- Constructors are private; use static factory methods (`create`, `reconstitute`)
- All state changes happen through explicit methods, never by setting properties directly
- State transition violations throw domain exceptions

### Value Objects

```typescript
// shared/domain/value-objects/money.ts
export class Money {
  private constructor(
    public readonly amount: number,
    public readonly currency: string,
  ) {}

  static of(amount: number, currency: string): Money {
    if (amount < 0) throw new InvalidMoneyError(amount);
    return new Money(amount, currency);
  }

  add(other: Money): Money { ... }
  equals(other: Money): boolean { ... }
}
```

**Rules:**
- Immutable — no setters
- Private constructor + static factory
- Implement `equals()`

### Domain Events

```typescript
// domain/events/ride-accepted.event.ts
export class RideAcceptedEvent extends BaseDomainEvent {
  constructor(
    public readonly rideId: string,
    public readonly driverId: string,
  ) {
    super();
  }
}
```

```typescript
// shared/domain/events/base-domain-event.ts
export abstract class BaseDomainEvent {
  public readonly occurredAt: Date = new Date();
  public readonly eventId: string = crypto.randomUUID();
}
```

### Repository Interfaces

```typescript
// domain/repositories/ride.repository.interface.ts
export interface IRideRepository {
  findById(id: RideId): Promise<Ride | null>;
  save(ride: Ride): Promise<void>;
  findActiveByPassengerId(passengerId: string): Promise<Ride | null>;
}

export const IRideRepository = Symbol('IRideRepository');
```

**Rule:** The interface lives in `domain/` — the implementation lives in `infrastructure/persistence/`.

### Commands, Queries, Handlers

```typescript
// application/commands/create-ride-request.command.ts
export class CreateRideRequestCommand {
  constructor(
    public readonly passengerId: string,
    public readonly pickupPoint: GeoCoordinates,
    public readonly destination: GeoCoordinates,
    public readonly rideType: 'immediate' | 'scheduled',
    public readonly scheduledAt?: Date,
  ) {}
}

// application/handlers/create-ride-request.handler.ts
@CommandHandler(CreateRideRequestCommand)
export class CreateRideRequestHandler
  implements ICommandHandler<CreateRideRequestCommand> {

  constructor(
    @Inject(IRideRepository) private readonly rides: IRideRepository,
    private readonly eventBus: EventBus,
  ) {}

  async execute(command: CreateRideRequestCommand): Promise<void> {
    const ride = Ride.create({ ...command });
    await this.rides.save(ride);
    ride.pullDomainEvents().forEach(e => this.eventBus.publish(e));
  }
}
```

---

## 7. Inter-Module Communication Rules

### The interface/ facade

Each module exposes a single facade class in `interface/`. Other modules inject this facade — they never import anything deeper.

```typescript
// src/modules/user-management/interface/user-management.interface.ts
@Injectable()
export class UserManagementInterface {
  constructor(private readonly queryBus: QueryBus) {}

  async getDriverProfile(driverId: string): Promise<DriverProfileDto | null> {
    return this.queryBus.execute(new GetDriverProfileQuery(driverId));
  }
}
```

### Domain events for async communication

When a module needs to react to something that happened in another module, it listens to a domain event — it does **not** call the other module's facade.

```typescript
// matching/application/handlers/ride-requested.handler.ts
@EventsHandler(RideRequestedEvent)
export class OnRideRequestedHandler implements IEventHandler<RideRequestedEvent> {
  async handle(event: RideRequestedEvent): Promise<void> {
    // trigger matching engine
  }
}
```

### Allowed dependency directions (from context map)

| Module | May call interface of |
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

Each module owns its own PostgreSQL schema. TypeORM entities in that module are prefixed with the schema name.

| Module | Schema |
|---|---|
| ride-management | `ride` |
| matching | `matching` |
| pricing | `pricing` |
| reputation | `reputation` |
| user-management | `users` |
| payment | `payment` |
| notification | `notification` |
| territorial-configuration | `territory` |
| geolocation | `geo` |

```typescript
// infrastructure/persistence/ride.orm-entity.ts
@Entity({ schema: 'ride', name: 'rides' })
export class RideOrmEntity {
  @PrimaryColumn('uuid') id: string;
  @Column() status: string;
  // ...
}
```

### Migrations

Each module manages its own migrations inside `infrastructure/persistence/migrations/`.
Run all migrations with `npm run migration:run`.

### Cross-module joins are forbidden

Modules **never** do SQL joins across schemas. If module A needs data from module B, it calls B's `interface/` facade. This keeps modules independently deployable in the future.

---

## 9. Redis & BullMQ Conventions

### Redis usage

| Use case | Key pattern | TTL |
|---|---|---|
| Driver real-time position | `geo:driver:{driverId}:position` | 30s |
| Available drivers index | `geo:available-drivers` (Redis GEO set) | — |
| Ride status cache | `ride:{rideId}:status` | 60s |

Use `GEOADD` / `GEORADIUS` Redis commands for proximity queries in the Matching module.

### BullMQ queues

| Queue name | Producer | Consumer | Purpose |
|---|---|---|---|
| `matching` | ride-management | matching | Trigger matching when a ride request is created |
| `notifications` | any module | notification | Send push/email/SMS asynchronously |
| `scheduled-rides` | ride-management | matching | Activate matching for scheduled rides near departure time |
| `pricing` | ride-management | pricing | Calculate fare estimate or final price |

```typescript
// Adding a job
await this.matchingQueue.add('match-ride', { rideId }, { attempts: 5, backoff: 3000 });
```

---

## 10. Mock External Services

All external services are **mocked** for the current implementation phase. Mocks live in each module's `infrastructure/adapters/` folder and implement the same interface as the real service would.

The real implementation can be swapped in later by replacing the adapter — the domain and application layers are never aware of the difference.

### Payment (Stripe mock)

```
payment/infrastructure/adapters/mock-payment.adapter.ts
```
- `charge(amount, currency, customerId)` → always returns `{ success: true, transactionId: 'mock-txn-xxx' }`
- `refund(transactionId, amount)` → always returns `{ success: true }`

### Routing & Maps (Google Maps mock)

```
geolocation/infrastructure/adapters/mock-routing.adapter.ts
```
- `calculateRoute(origin, destination)` → returns a fixed distance (km) and duration (min) based on straight-line approximation
- `getEta(driverPosition, pickupPoint)` → returns a fixed 5-minute ETA

### Push Notifications (Firebase Cloud Messaging mock)

```
notification/infrastructure/adapters/mock-push.adapter.ts
```
- `send(deviceToken, title, body)` → logs to console: `[MOCK PUSH] → {deviceToken}: {title}`

### Email (Resend mock)

```
notification/infrastructure/adapters/mock-email.adapter.ts
```
- `send(to, subject, html)` → logs to console: `[MOCK EMAIL] → {to}: {subject}`

### SMS (Twilio mock)

```
notification/infrastructure/adapters/mock-sms.adapter.ts
```
- `send(phoneNumber, message)` → logs to console: `[MOCK SMS] → {phoneNumber}: {message}`

### How to wire a mock

```typescript
// notification/notification.module.ts
@Module({
  providers: [
    { provide: IPushNotificationService, useClass: MockPushAdapter },
    { provide: IEmailService,            useClass: MockEmailAdapter },
    { provide: ISmsService,              useClass: MockSmsAdapter },
  ],
})
export class NotificationModule {}
```

When a real adapter is ready, replace `MockPushAdapter` with `FcmAdapter` — nothing else changes.

---

## 11. Testing Conventions

### Unit tests

- Location: `tests/unit/<module-name>/`
- Test domain logic in isolation — **no database, no NestJS, no HTTP**
- Mock all dependencies with Jest mocks
- File naming: `<subject>.spec.ts`

```typescript
// tests/unit/ride-management/ride.entity.spec.ts
describe('Ride', () => {
  it('should move to Accepted when Proposed', () => {
    const ride = Ride.reconstitute({ status: RideStatus.Proposed, ... });
    ride.accept();
    expect(ride.status).toBe(RideStatus.Accepted);
  });

  it('should throw on invalid transition', () => {
    const ride = Ride.reconstitute({ status: RideStatus.Finalized, ... });
    expect(() => ride.accept()).toThrow(InvalidTransitionError);
  });
});
```

### Integration tests

- Location: `tests/integration/<module-name>/`
- Spin up a real PostgreSQL + Redis via Docker (use `testcontainers` or a shared test DB)
- Test the full application layer: command → handler → repository → database
- Use the mock adapters for external services

### E2E / BDD tests

- Location: `tests/e2e/`
- Driven by the `.feature` files in `features/`
- Use **Cucumber.js** with step definitions mapping to HTTP calls against the running app
- All external services use mock adapters

```
features/ride_management.feature  →  tests/e2e/steps/ride-management.steps.ts
```

### Coverage targets

| Layer | Target |
|---|---|
| Domain (entities, value objects) | 100% |
| Application (handlers) | > 80% |
| Infrastructure (adapters, persistence) | integration tests only |

---

## 12. Bounded Context Reference Sheet

Use this section as a quick reference when implementing any module. Each entry defines what the module owns, what it exposes, what events it emits, and what events it listens to.

---

### ride-management ⭐ Core Domain

| | |
|---|---|
| **Owns** | Ride, RideRequest, RideOffer, RideStatus state machine |
| **Exposes** | `createRideRequest`, `createRideOffer`, `getRideById`, `getRidesByPassenger` |
| **Emits** | `RideRequestedEvent`, `RideOfferedEvent`, `RideAcceptedEvent`, `RidePickedUpEvent`, `RideInProgressEvent`, `RideArrivedEvent`, `RideFinalizedEvent`, `RideCancelledEvent`, `RideIncidentEvent` |
| **Listens to** | `PaymentProcessedEvent` (to finalize), `MatchFoundEvent` (to move to Proposed) |
| **External mocks** | none |

---

### matching

| | |
|---|---|
| **Owns** | Match, Grouping, MatchingRules |
| **Exposes** | `getMatchForRide` |
| **Emits** | `MatchFoundEvent`, `MatchFailedEvent`, `GroupingCreatedEvent` |
| **Listens to** | `RideRequestedEvent` (triggers matching), `RideOfferedEvent` (triggers offer search) |
| **Depends on (via interface/)** | `user-management`, `geolocation`, `territorial-configuration` |
| **External mocks** | none |

---

### pricing

| | |
|---|---|
| **Owns** | FareEstimate, FinalPrice, DynamicPricingRules |
| **Exposes** | `calculateFareEstimate`, `calculateFinalPrice`, `calculateRideOfferPricePerSeat` |
| **Emits** | `FareCalculatedEvent` |
| **Listens to** | `RideRequestedEvent` (estimate), `RideArrivedEvent` (final price) |
| **Depends on (via interface/)** | `geolocation`, `territorial-configuration` |
| **External mocks** | none |

---

### reputation

| | |
|---|---|
| **Owns** | Rating, ReputationScore, Penalty, Restriction |
| **Exposes** | `getReputationScore`, `isEligible` |
| **Emits** | `ReputationUpdatedEvent`, `PenaltyAppliedEvent`, `RestrictionAppliedEvent`, `RestrictionLiftedEvent` |
| **Listens to** | `RideFinalizedEvent` (open rating window), `RideCancelledEvent` (check penalty threshold) |
| **Depends on (via interface/)** | `user-management` |
| **External mocks** | none |

---

### user-management

| | |
|---|---|
| **Owns** | User, IndividualAccount, ProfessionalAccount, DriverProfile, VehicleProfile, Availability |
| **Exposes** | `getUserById`, `getDriverProfile`, `getAvailableDriversNear`, `isDriverAvailable`, `setAvailability` |
| **Emits** | `DriverAvailabilityChangedEvent`, `DriverProfileValidatedEvent`, `AccountRestrictedEvent` |
| **Listens to** | `RestrictionAppliedEvent`, `RestrictionLiftedEvent` |
| **External mocks** | none |

---

### payment

| | |
|---|---|
| **Owns** | Payment, PaymentMethod, Transaction, Refund |
| **Exposes** | `processPayment`, `refund`, `getPaymentStatus` |
| **Emits** | `PaymentProcessedEvent`, `PaymentFailedEvent`, `RefundProcessedEvent` |
| **Listens to** | `RideArrivedEvent` (trigger payment) |
| **External mocks** | `MockPaymentAdapter` (Stripe) — always succeeds |

---

### notification

| | |
|---|---|
| **Owns** | Notification, NotificationChannel, NotificationPreference |
| **Exposes** | none (purely reactive — listens to events) |
| **Emits** | none |
| **Listens to** | `RideAcceptedEvent`, `RideCancelledEvent`, `RideFinalizedEvent`, `MatchFoundEvent`, `PaymentFailedEvent`, `RestrictionAppliedEvent`, `RestrictionLiftedEvent` |
| **External mocks** | `MockPushAdapter` (FCM), `MockEmailAdapter` (Resend), `MockSmsAdapter` (Twilio) |

---

### territorial-configuration

| | |
|---|---|
| **Owns** | Territory, TerritorialRule, RegulatoryConstraint |
| **Exposes** | `getRulesForTerritory`, `getTerritoryForCoordinates`, `isCoordinatesCovered` |
| **Emits** | none |
| **Listens to** | none |
| **External mocks** | none |

---

### geolocation

| | |
|---|---|
| **Owns** | RealTimePosition, Route, ETA |
| **Exposes** | `calculateRoute`, `getEta`, `updateDriverPosition`, `getDriversWithinRadius` |
| **Emits** | `DriverPositionUpdatedEvent` |
| **Listens to** | `DriverAvailabilityChangedEvent` (start/stop tracking) |
| **External mocks** | `MockRoutingAdapter` (Google Maps) — returns approximated values |
| **Note** | Uses Redis GEO commands for real-time position storage. PostGIS for persistent route data. Extraction candidate if performance demands it. |

---

> *Last updated: 2026-04-28*
> *Any change to this document must be discussed with the team — it affects all bounded contexts.*
