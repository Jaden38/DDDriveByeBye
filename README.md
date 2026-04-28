# DDDriveByeBye

VTC & Carpooling Mobility Platform — built with Domain-Driven Design.

## Getting Started

```bash
# Install dependencies
npm install

# Start PostgreSQL + Redis
docker compose up -d postgres redis

# Copy env and start
cp .env.example .env
npm run start:dev
```

App runs at `http://localhost:3000`.

To run everything in Docker:
```bash
docker compose up
```

## Tech Stack

| | |
|---|---|
| Framework | NestJS 10 + TypeScript 5 |
| Database | PostgreSQL 15 + PostGIS |
| Cache / Queues | Redis 7 + BullMQ |
| Architecture | Modular Monolith + DDD |

## Project Structure

```
src/
├── modules/
│   ├── ride-management/        # Core domain — ride lifecycle, state machine
│   ├── matching/               # Matching engine + carpooling grouping
│   ├── pricing/                # Dynamic pricing, surge, fare estimates
│   ├── reputation/             # Ratings, reputation scores, penalties
│   ├── user-management/        # Users, driver profiles, availability
│   ├── payment/                # Payment processing (Stripe)
│   ├── notification/           # Push, email, SMS notifications
│   ├── territorial-configuration/ # Territory rules & regulatory constraints
│   └── geolocation/            # Real-time positions, routing, ETA
├── shared/                     # Shared kernel (value objects, base events)
└── app/                        # Bootstrap & config
features/                       # Gherkin BDD scenarios
documentation/                  # Architecture & domain docs
tests/                          # Unit, integration, e2e
```

## Documentation

| Document | Description |
|---|---|
| [`documentation/UBIQUITOUS_LANGUAGE.md`](documentation/UBIQUITOUS_LANGUAGE.md) | Shared domain vocabulary |
| [`documentation/technical_specs.md`](documentation/technical_specs.md) | Tech stack, conventions, module contracts |
| [`documentation/context_map.png`](documentation/context_map.png) | Bounded context relationships |
| [`features/`](features/) | BDD scenarios per bounded context |

## Scripts

```bash
npm run start:dev       # Dev server with hot reload
npm run build           # Production build
npm run test            # Unit tests
npm run migration:run   # Run database migrations
```

## Account Types

| | Individual | Professional |
|---|---|---|
| Passenger role | yes | no |
| Immediate rides (driver) | yes | yes |
| Scheduled rides (driver) | yes | no |
| Publish ride offers | yes | no |
| Working zone constraint | soft (preference) | hard (required) |
