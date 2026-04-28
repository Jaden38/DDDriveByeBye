# Bounded Context: SUPPORTING DOMAIN — Matching
# Handles matching in both directions:
#   - Ride Request flow: finds a driver for a passenger's request (Immediate & Scheduled Rides)
#   - Ride Offer flow: matches a passenger's search to existing published Ride Offers (Scheduled Rides)
# Account type rules enforced by the matching engine:
#   - Immediate and Scheduled Ride requests → both Individual and Professional drivers eligible
#   - Ride Offer searches                   → Individual drivers only (carpooling)
# Conforms to: Territorial Configuration, Geolocation & Routing
# Customer-Supplier with: User Management

Feature: Ride Search Matching (Ride Offer flow)
  As the matching engine
  I want to match a passenger's ride search to compatible published ride offers
  So that passengers can join existing rides before submitting a new ride request
  # Ride Offers are published by Individual drivers only (carpooling)

  Background:
    Given an Individual user acting as a passenger has submitted a ride search
    And the search specifies an origin, a destination, and a departure date
    And the territory is covered by the platform

  Scenario: Passenger matched to a compatible ride offer
    Given the following ride offers are published
      | Driver  | Departure origin  | Destination         | Date       | Time  | Seats | Price (system-calculated) |
      | Jean    | Lyon Part-Dieu    | Paris Gare de Lyon  | 2026-05-10 | 08:00 | 3     | 25.00€                    |
      | Paul    | Lyon Perrache     | Paris Bercy         | 2026-05-10 | 09:30 | 1     | 22.00€                    |
    And the passenger searches for "Lyon → Paris" on "2026-05-10"
    When the matching engine runs the ride search
    Then both ride offers are returned as compatible results
    And results are ranked by departure time proximity to the passenger's preferred time

  Scenario: Ride offer excluded from search results when full
    Given the ride offer by "Jean" has status "Full"
    When the matching engine runs the ride search
    Then "Jean"'s ride offer is excluded from results

  Scenario: Ride offer excluded due to incompatible ride options
    Given the passenger's ride search requires the ride option "wheelchair accessible"
    And the ride offer by "Jean" does not support "wheelchair accessible"
    When the matching engine runs the ride search
    Then "Jean"'s ride offer is excluded from results due to incompatible vehicle profile

  Scenario: No ride offer found — passenger falls back to ride request
    Given no published ride offer matches the passenger's search criteria
    When the matching engine finds no results
    Then the passenger is informed "No ride offer found for your search"
    And the passenger is offered the option to submit a ride request as a fallback


Feature: Driver Matching (Ride Request flow)
  As the matching engine
  I want to find the most suitable available driver for a ride request
  So that the ride is assigned quickly and optimally

  Background:
    Given a ride request with status "Requested" exists
    And the pickup point is within a covered territory

  Scenario: Successful match with the nearest available driver
    Given the following drivers are available near the pickup point
      | Driver  | Account Type  | Distance | Reputation Score | Restrictions |
      | Jean    | Professional  | 1.2 km   | 4.8              | none         |
      | Paul    | Individual    | 2.5 km   | 4.5              | none         |
      | Marc    | Professional  | 0.9 km   | 2.9              | Suspension   |
    When the matching engine runs
    Then a ride proposal is sent to driver "Jean" (nearest eligible driver)
    And driver "Marc" is excluded due to active Suspension restriction
    And the ride moves to status "Proposed"
    And a 30-second response window is assigned to driver "Jean"

  Scenario: Driver ranked by proximity when reputation scores are equal
    Given the following drivers are available near the pickup point
      | Driver  | Distance | Reputation Score |
      | Jean    | 3.0 km   | 4.5              |
      | Paul    | 1.5 km   | 4.5              |
    When the matching engine runs
    Then a ride proposal is sent to driver "Paul" (closest among equals)

  Scenario: Compatibility check against ride options
    Given the ride request includes the ride options "child seat" and "wheelchair accessible"
    And the following drivers are available
      | Driver  | Distance | Supports child seat | Supports wheelchair accessible |
      | Jean    | 1.0 km   | yes                 | no                             |
      | Paul    | 2.0 km   | yes                 | yes                            |
    When the matching engine runs
    Then a ride proposal is sent to driver "Paul"
    And driver "Jean" is excluded due to incompatible vehicle profile

  Scenario: Compatibility check against driver preferences
    Given the ride request includes a passenger with a pet
    And the following drivers are available
      | Driver  | Distance | Accepts pets |
      | Jean    | 1.0 km   | no           |
      | Paul    | 2.0 km   | yes          |
    When the matching engine runs
    Then a ride proposal is sent to driver "Paul"
    And driver "Jean" is excluded due to driver preference mismatch

  Scenario: Activity zone filter applied during matching
    Given the ride pickup point is in the "15th arrondissement, Paris"
    And driver "Jean" has defined their preferred activity zone as "15th - 16th arrondissement"
    And driver "Paul" has no preferred activity zone defined
    And both drivers are equally distant from the pickup point
    When the matching engine runs
    Then driver "Jean" is prioritized over driver "Paul"

  Scenario: No driver available in the area
    Given no driver is available within a 10 km radius of the pickup point
    When the matching engine runs
    Then the ride request remains in status "Requested"
    And the passenger is notified "No driver available at the moment, retrying..."
    And the matching engine retries after a configured interval

  Scenario: Ride proposal expires — rematching triggered
    Given a ride proposal has been sent to driver "Jean"
    When driver "Jean" does not respond within 30 seconds
    Then the proposal expires
    And the ride falls back to status "Requested"
    And the matching engine runs again excluding driver "Jean" for this request

  Scenario: Driver declines the ride proposal — rematching triggered
    Given a ride proposal has been sent to driver "Jean"
    When driver "Jean" declines the proposal
    Then the ride falls back to status "Requested"
    And the matching engine runs again excluding driver "Jean" for this request

  Scenario: Maximum rematch attempts reached
    Given a ride request has been rejected or expired 5 times
    And no other eligible driver is available
    When the matching engine runs again
    Then the ride request is marked as unmatched
    And the passenger is notified "We could not find a driver for your request. Please try again later."

  Scenario: Matching respects territorial rules
    Given the territory "Paris - Île-de-France" requires a "VTC license"
    And driver "Jean" does not have a VTC license
    And driver "Paul" has a valid VTC license
    When the matching engine runs for a request in this territory
    Then driver "Jean" is excluded from the results
    And driver "Paul" is eligible for the ride proposal

  Scenario: Scheduled ride matched close to departure time
    Given a scheduled ride request planned for "2026-05-01 09:00"
    And it is currently "2026-05-01 08:45"
    When the matching engine processes scheduled rides
    Then the matching engine activates for this ride request
    And a ride proposal is sent to the nearest eligible driver


Feature: Carpooling Grouping
  As the matching engine
  I want to group passengers with compatible routes on the same ride
  So that carpooling is optimized for both passengers and drivers

  Background:
    Given carpooling grouping is enabled in the current territory

  Scenario: Two passengers grouped on the same ride
    Given the passenger "Alice" has a ride request with destination "Montparnasse"
    And the passenger "Charlie" has a ride request with destination "Montparnasse"
    And both pickup points are within 500 m of each other
    And driver "Jean" is available with 2 seats free
    When the grouping engine runs
    Then a grouping of "Alice" and "Charlie" is created
    And a shared ride proposal is sent to driver "Jean"
    And each passenger's fare is reduced compared to an individual ride
    And both passengers are informed of the grouping before confirming

  Scenario: Grouping rejected when routes are incompatible
    Given the passenger "Alice" has a destination "Montparnasse"
    And the passenger "Bob" has a destination "CDG Airport"
    When the grouping engine evaluates compatibility
    Then no grouping is created
    And each ride request is matched independently

  Scenario: Grouping rejected when driver has insufficient seats
    Given the passenger "Alice" and the passenger "Charlie" are candidates for grouping
    And driver "Jean" only has 1 seat available
    When the grouping engine runs
    Then no grouping is created
    And ride requests are matched independently

  Scenario: Passenger opts out of carpooling grouping
    Given the passenger "Alice" has disabled carpooling in her preferences
    When the grouping engine evaluates her ride request
    Then "Alice"'s request is excluded from all grouping candidates
    And her ride request is matched with an individual driver only

  Scenario: Grouping disbanded when one passenger cancels
    Given a grouping of "Alice" and "Charlie" has been formed
    And the ride is in status "Proposed"
    When "Alice" cancels her ride request
    Then the grouping is dissolved
    And "Charlie"'s ride request re-enters the matching engine as an individual request
    And driver "Jean" receives an updated ride proposal
