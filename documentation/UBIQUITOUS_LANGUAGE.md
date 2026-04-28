# Ubiquitous Language — VTC & Carpooling Mobility Platform

---

## Actors & Profiles

**User**: Any person registered on the platform, regardless of their role. A user can be a Passenger, a Driver, or both. This term is used when a rule or feature applies equally to both roles (e.g., rating, penalty, suspension).

**Passenger**: User who submits a ride request to travel from one point to another. They may have specific requirements (luggage, number of seats, specific options).

**Driver**: User who offers transportation services with their vehicle. They have a profile defining their availability, activity zones, ride preferences and vehicle characteristics.

**Activity Zone**: Geographic area in which a driver agrees to operate. A driver can have one or more activity zones.

**Vehicle Profile**: Set of characteristics of a vehicle registered by a driver (type, number of available seats, options such as air conditioning, pet transport, wheelchair accessibility, etc.).

**Availability**: Time slot during which a driver declares themselves ready to accept rides.

---

## Ride Request & Matching

**Ride Request**: Request submitted by a passenger indicating a pickup point, a destination, and optional constraints (desired time, number of seats, luggage, options). This is the triggering event for the entire process.

**Immediate Ride**: Ride request to be fulfilled as quickly as possible by assigning an available driver nearby (VTC / on-demand transport mode).

**Scheduled Ride**: Ride request planned in advance for a future date and time (scheduled carpooling mode).

**Match**: Result of the matching process between a ride request and one or more compatible drivers, taking into account location, destination, preferences and constraints.

**Grouping**: Pooling of multiple passengers with similar or compatible routes on the same ride (carpooling). The system identifies route overlaps to optimize grouping.

**Ride Proposal**: Offer sent to one or more drivers matching a request. The driver can accept or decline within a given time limit.

**Assignment**: Definitive association of a driver to a ride request following acceptance of a proposal.

---

## Ride Lifecycle

**Ride Status**: Current state of a ride in its lifecycle. Possible statuses are:

- **Requested**: The passenger has submitted their request; no proposal has been sent yet.
- **Proposed**: A proposal has been sent to one or more drivers, awaiting response.
- **Accepted**: A driver has accepted the proposal; the ride is confirmed.
- **Picked Up**: The driver has arrived at the pickup point and the passenger has boarded.
- **In Progress**: The vehicle is moving toward the destination.
- **Arrived**: The passenger has been dropped off at the destination; the physical ride is complete.
- **Finalized**: Payment has been processed and the ride is closed.
- **Cancelled**: The ride has been cancelled before completion by one of the parties.
- **Incident**: An unexpected event has disrupted the normal course of the ride.

**Transition Rule**: Business constraint that defines the conditions under which a ride can move from one status to another (e.g., a ride can only move to "Picked Up" if its current status is "Accepted").

**Cancellation**: Voluntary interruption of a ride by the passenger or driver before finalization. Cancellation may be subject to conditions (time limits, penalties) depending on when it occurs.

**Incident**: Unexpected event occurring during a ride (accident, breakdown, inappropriate behavior, etc.) requiring specific handling.

**Withdrawal**: Case where a driver who has accepted a ride retracts before pickup. This triggers a new match search.

---

## Pricing

**Dynamic Pricing**: Pricing mechanism that adjusts ride prices in real time based on multiple criteria.

**Pricing Factors**: Set of criteria influencing the price: distance, estimated duration, real-time demand, driver availability, traffic conditions, geographic zone, territorial rules.

**Surge**: Fare increase applied under certain conditions (high demand, night hours, public holidays, etc.).

**Discount**: Fare reduction applied according to commercial rules (loyalty, referral, etc.).

**Promotional Offer**: Temporary pricing advantage offered to passengers based on criteria defined by the commercial strategy (promo code, first ride, etc.).

**Fare Estimate**: Indicative price communicated to the passenger at the time of the ride request, before confirmation. This amount may differ from the final price in case of route or condition changes.

**Final Price**: Definitive amount charged to the passenger at the end of the ride, calculated based on the ride actually completed.

---

## Payment

**Payment**: Financial transaction made by the passenger at the end of a ride to settle the final price.

**Payment Method**: Method used by the passenger to pay (credit card, electronic wallet, etc.).

---

## Reputation & Rating

**Rating**: Score and/or comment given by a passenger to a driver (or vice versa) at the end of a ride. Ratings are reciprocal.

**Reputation Score**: Aggregated indicator reflecting the quality and reliability of a passenger or driver, calculated from their rating history.

**Penalty**: Sanction applied to a passenger or driver in case of inappropriate behavior (repeated cancellations, frequent delays, recurring poor ratings). Can take the form of a warning, access restriction, or suspension.

**Restriction**: Limited access to certain platform features applied to a user whose behavior is deemed problematic (e.g., being unable to receive proposals for a given period).

**Suspension**: Temporary or permanent exclusion of a user from the platform; the highest degree of penalty.

**Reputation Threshold**: Minimum reputation score value below which penalties or restrictions are automatically triggered.

---

## Geolocation & Routing

**Pickup Point**: Location where the passenger wishes to be picked up.

**Destination**: Location where the passenger wishes to be dropped off.

**Route**: Geographic path calculated between the pickup point and the destination, taking traffic conditions into account.

**Estimated Time of Arrival (ETA)**: Expected duration to complete a given ride, recalculated based on real-time traffic conditions.

**Traffic Conditions**: Current state of road traffic at a given moment, influencing the ETA and potentially the pricing.

**Real-Time Position**: Current geographic location of a driver or an ongoing ride, used for tracking and matching.

---

## Territorial Configuration

**Territory**: Geographic area (city, region, country) associated with specific operational, regulatory and pricing rules.

**Territorial Rule**: Business constraint or parameter specific to a given territory (e.g., minimum fare imposed by local regulation, specific insurance requirement, surge cap).

**Regulatory Constraint**: Legal obligation applicable in a given territory that influences platform operations (VTC license, insurance, driver working conditions, etc.).

**Territorial Configuration**: Set of parameters (pricing rules, regulatory constraints, available options) that define the platform's behavior in a given territory.

---

## Preferences & Options

**Passenger Preference**: Requirement or wish expressed by a passenger for a ride (e.g., quiet vehicle, minimal conversation, temperature, music).

**Driver Preference**: Wish expressed by a driver regarding the rides they accept (e.g., short rides only, no pets, preferred zone).

**Ride Option**: Additional feature requested for a ride (e.g., child seat, oversized luggage transport, wheelchair accessibility). Available options may vary by territory.

---

> *This language constitutes the shared vocabulary between all project stakeholders (developers, product owners, domain experts, client). Every term used in code, user stories, tests and discussions must refer to this glossary to ensure a common and unambiguous understanding.*
