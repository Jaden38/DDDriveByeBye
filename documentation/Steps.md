---
Task 1 — Project Bootstrap + User Management + Territorial Configuration ✅ DONE (tests deferred)

Why together: Zero upstream dependencies — unblocks everyone else. Must be done first.

Stack note: implemented in Java 21 / Spring Boot 3.3 (not NestJS as originally planned). TypeORM → Spring Data JPA + Flyway. The DDD layering (`domain` / `application` / `infrastructure` / `api`) and inter-module rules from TECHNICAL_SPECS.md still apply.

- ✅ Spring Boot project, Docker Compose (postgres + redis + app, healthchecked), shared kernel (`Money`, `GeoCoordinates`, `DateRange`, `BaseDomainEvent`, `DomainEventPublisher`)
- ✅ user-management module — Individual/Professional accounts, driver profile lifecycle, vehicle profile, availability, activity zone & working zone, domain events (`DriverAvailabilityChangedEvent`, `DriverProfileValidatedEvent`, `DriverProfileRejectedEvent`, `AccountRestrictedEvent`)
- ✅ territorial-configuration module — `Territory` aggregate, `TerritorialRule`, `RegulatoryConstraint`, `GeographicZone`
- ✅ Public facades exported (`UserManagementFacade`, `TerritorialConfigurationFacade`) — other modules may depend on these immediately and mock them
- ✅ Flyway migration `V1__init.sql` — PostGIS extension, `users` and `territory` schemas, all tables and indexes
- ✅ REST controllers at `/api/users` and `/api/territories` (smoke-tested end-to-end)
- ⏳ Tests deferred — TECHNICAL_SPECS.md requires 100% on domain, >80% on application handlers, plus Cucumber bindings for `features/user_management.feature` and `features/territorial_configuration.feature`. To be picked up before closing Step 1.

---
Task 2 — Geolocation ✅ DONE

Why alone: Technically distinct (Redis GEO + PostGIS), foundational for Matching and Pricing.
- ✅ geolocation module — `RealTimePosition` aggregate (Redis GEO commands via `RedisDriverPositionRepository`), `Route` aggregate (PostGIS-backed `geo.routes` table), `Eta` value object
- ✅ `MockRoutingAdapter` (haversine distance, 30 km/h average duration, fixed 5-min ETA)
- ✅ `GeolocationFacade` exposes `updateDriverPosition`, `removeDriverPosition`, `getDriverPosition`, `getDriversWithinRadius`, `calculateRoute`, `getEta`
- ✅ REST controller at `/api/geolocation`
- ✅ `DriverAvailabilityChangedEvent` listener removes the driver from the GEO set when going OFFLINE
- ✅ Flyway migration `V2__geolocation.sql` — `geo` schema + `geo.routes` with PostGIS GIST index
- ✅ Unit tests (39 tests, all green) — domain VOs (`Eta`, `DriverId`, `RouteId`), domain entities (`RealTimePosition`, `Route`), `MockRoutingAdapter`, and application handlers (`GeolocationCommandHandler`, `GeolocationQueryHandler`, `DriverAvailabilityEventHandler`)
- ⏳ Cucumber bindings for `features/geolocation.feature` still pending (consistent with Task 1)

---
Task 3 — Ride Management (Core Domain)

Why alone: The most complex module — state machine with 9 statuses, two initiator flows (RideRequest + RideOffer), all domain events that the rest of the system
reacts to.
- ride-management module — Ride aggregate, transition rules, RideRequest flow, RideOffer flow
- All domain events (RideRequestedEvent, RideAcceptedEvent, RideFinalizedEvent, etc.)
- BullMQ producers (enqueue matching, pricing jobs)

---
Task 4 — Matching ✅ DONE (tests deferred)

Why alone: Algorithmically the most complex supporting domain — depends on Task 1 + 2 interfaces.

- ✅ matching module — `Match` aggregate with state machine (`SEARCHING` → `PROPOSED` → `ACCEPTED`/`UNMATCHED`/`CANCELLED`), 30-second proposal window, 5-attempt rematch ceiling, exclusion of declined drivers
- ✅ Immediate Ride matching — Individual + Professional eligible; Professional Working-Zone hard filter; Individual Activity-Zone soft prioritisation (`DriverRanking` domain service: zone, then reputation, then proximity)
- ✅ Scheduled Ride matching — Individual drivers only (`RideKind.SCHEDULED` filters Professional out at the user-management facade call)
- ✅ Grouping engine — `Grouping` aggregate; opt-in carpooling, shared destination, configurable pickup-proximity radius, seat capacity check; dissolved on passenger cancellation
- ✅ Ride-Offer search — published-offer query via `RideOfferCatalogPort`, ranked by departure-time proximity, excludes "Full" and option-incompatible offers
- ✅ Public facade `MatchingFacade` with command (`run/accept/decline/expire/cancel`, grouping eval/dissolve) and query methods (`getMatchForRide`, `searchRideOffers`)
- ✅ REST controller at `/api/matching`
- ✅ Domain events (`MatchProposalSentEvent`, `MatchFoundEvent`, `MatchFailedEvent`, `GroupingCreatedEvent`, `GroupingDissolvedEvent`) drained from aggregates and published via `SpringDomainEventPublisher`
- ✅ Persistence in `matching` schema — Flyway `V2__matching.sql`, tables `matches` / `match_exclusions` / `groupings` / `grouping_members`
- ✅ Ports for not-yet-implemented modules — `GeolocationPort`, `ReputationPort`, `RideOfferCatalogPort` with stub adapters that activate by default and step aside under explicit profiles (`geolocation-real`, `reputation-real`, `ride-management-real`)
- ⏳ Tests deferred — domain unit tests, application handler tests with mocked repositories/facades, and Cucumber bindings against `features/matching.feature`. To be picked up before closing Step 4.
- ⏳ BullMQ queue consumer not implemented; the facade is invoked synchronously for now. To be wired once an async dispatcher is introduced.

---
Task 5 — Pricing + Payment + Reputation + Notification

Why together: All four are largely reactive (listen to events from Ride Management) and two of them are almost entirely mocked — the combined load is comparable to a
single complex module.
- pricing — dynamic fare, surge coefficient, promotional offers, final price calculation
- payment — payment processing, refunds (MockPaymentAdapter for Stripe)
- reputation — mutual ratings, reputation score calculation, penalties, restrictions, reputation thresholds
- notification — event listeners for all ride/payment/reputation events, MockPushAdapter, MockEmailAdapter, MockSmsAdapter

---
Dependency order to keep in mind

Task 1 (User Mgmt + Territory)  ──┐
Task 2 (Geolocation)             ─┼──▶  Task 4 (Matching)
Task 3 (Ride Management Core)   ──┘         │
▼
Task 5 (Pricing + Payment
+ Reputation + Notification)

Tasks 1, 2, and 3 can start in parallel on day one. Task 4 needs the interface/ facades from 1 and 2 — those should be defined within the first day or two even if
the full implementation isn't ready. Task 5 needs domain events from Task 3 to be defined, which should come early since events are part of the domain layer.