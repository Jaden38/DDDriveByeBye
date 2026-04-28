# Ubiquitous Language — VTC & Carpooling Mobility Platform

---

## Actors & Profiles

**User**: Any person registered on the platform. A user has one of two account types — Individual or Professional — which determines which roles they can take and which ride modes they can access. Rules that apply equally to all users regardless of type (e.g., rating, penalty, suspension) use this term.

**Individual Account**: Account type for carpooling. An individual user can act as both a Passenger and a Driver, but never simultaneously on the same ride. As a Passenger, they can submit Immediate Ride Requests, submit Scheduled Ride Requests, or search for and join existing Ride Offers. As a Driver, they can activate their availability to pick up Immediate Ride passengers, or publish Ride Offers for planned trips.

**Professional Account**: Account type for on-demand VTC transport. A professional user is a driver only — they cannot act as a Passenger, cannot publish Ride Offers, and cannot handle Scheduled Rides. They operate exclusively on Immediate Rides within their defined Working Zone. A Professional Account requires a VTC license and undergoes a stricter validation process.

**Passenger**: Role taken by an Individual user when they are looking for or taking a ride. A user acting as a Passenger can search for existing Ride Offers or submit a Ride Request. They must deactivate their driver availability before acting as a Passenger. Professional accounts cannot take this role.

**Driver**: Role taken by a user (Individual or Professional) when they are offering or driving a ride. An Individual driver can publish Ride Offers or respond to Ride Requests for Scheduled Rides. A Professional driver responds to Immediate Ride Requests only. A user cannot act as a Driver and a Passenger at the same time on the same ride.

**Activity Zone**: Preferred geographic area defined by an Individual driver. Used as a soft filter in matching — rides outside the zone are deprioritized but still reachable.

**Working Zone**: Required geographic area defined by a Professional driver. Used as a hard constraint in matching — Professional drivers only receive Immediate Ride proposals within their Working Zone.

**Vehicle Profile**: Set of characteristics of a vehicle registered by a driver (type, number of available seats, options such as air conditioning, pet transport, wheelchair accessibility, etc.).

**Availability**: Time slot during which a driver declares themselves ready to accept rides.

---

## Ride Offer & Ride Request

**Ride Offer**: A ride published proactively by an Individual user acting as a Driver, specifying a planned route, departure date and time, and available seats. The price per seat is calculated automatically by the Dynamic Pricing system at publication time based on route, territorial rules, and current conditions — the driver does not set it manually. Other Individual users acting as Passengers can search for and join a Ride Offer. Ride Offers are specific to Scheduled Rides (carpooling mode). Professional accounts cannot publish or join Ride Offers.

**Ride Request**: Request submitted by a user acting as a Passenger, indicating a pickup point, a destination, and optional constraints (desired time, number of seats, luggage, options). For Scheduled Rides, a Ride Request is typically submitted after no suitable Ride Offer was found. For Immediate Rides, it is always the starting point.

**Ride Search**: The action performed by a user acting as a Passenger to browse available Ride Offers before deciding to submit a Ride Request. A Ride Search is only applicable for Scheduled Rides.

**Immediate Ride**: A Ride Request to be fulfilled as quickly as possible by assigning a nearby available driver (on-demand mode). Always passenger-initiated. Both Individual and Professional drivers are eligible. Professional drivers are only matched within their Working Zone.

**Scheduled Ride**: A ride planned in advance for a future date and time (carpooling mode). Involves Individual users only. Can be driver-initiated via a Ride Offer, or passenger-initiated via a Ride Request when no suitable Ride Offer is found. Professional drivers do not participate in Scheduled Rides.

**Match**: Result of the matching process between a Ride Request or Ride Offer and compatible counterparts, taking into account location, destination, preferences, and constraints.

**Grouping**: Pooling of multiple passengers with similar or compatible routes on the same ride (carpooling). The system identifies route overlaps to optimize grouping.

**Ride Proposal**: Notification sent to a user to confirm or finalise a match. In the Ride Request flow, it is sent to a Driver. In the Ride Offer flow, it is sent to the Passenger to confirm their seat reservation. The recipient can accept or decline within a given time limit.

**Assignment**: Definitive association of a driver to a ride, or of a passenger to a Ride Offer seat, following acceptance of a Ride Proposal.

---

## Ride Lifecycle

**Ride Status**: Current state of a ride in its lifecycle. Possible statuses are:

- **Requested**: The passenger has submitted a Ride Request; no proposal has been sent yet. (Ride Request flow only.)
- **Published**: The driver has published a Ride Offer; no passenger has joined yet. (Ride Offer flow only.)
- **Proposed**: A proposal has been sent awaiting response — either to a driver (Ride Request flow) or to a passenger confirming their seat (Ride Offer flow).
- **Accepted**: The proposal has been accepted; the ride is confirmed.
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

**Dynamic Pricing**: Pricing mechanism that adjusts ride prices in real time based on multiple criteria. Applies to all ride types — Immediate Rides, passenger-initiated Scheduled Rides, and Ride Offers. The driver never sets the price manually; the pricing system always owns fare calculation.

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
