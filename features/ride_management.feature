# Bounded Context: CORE DOMAIN — Ride Management
# Manages the complete ride lifecycle and state machine.
# Two initiator flows feed into the same lifecycle:
#   - Ride Request flow: passenger-initiated, always for Immediate Rides, fallback for Scheduled Rides
#   - Ride Offer flow:   driver-initiated, Scheduled Rides only
# Account type rules:
#   - Immediate Rides         → Individual AND Professional drivers
#   - Scheduled Ride Requests → Individual drivers only
#   - Ride Offers             → Individual drivers only
#   - Passenger role          → Individual accounts only

Feature: Ride Request
  As a user acting as a passenger
  I want to submit a ride request
  So that I can be matched with an available driver

  Background:
    Given the user "Alice" is logged in with an active account
    And "Alice" has no active restriction
    And "Alice" is not currently acting as a driver on an ongoing ride
    And a pickup point "10 rue de la Paix, Paris"
    And a destination "Gare du Nord, Paris"

  Scenario: Successful immediate ride request
    When "Alice" submits an immediate ride request
    Then a ride request is created with status "Requested"
    And a fare estimate is calculated and displayed to "Alice"
    And the matching engine is notified of the new ride request

  Scenario: Scheduled ride request submitted after no matching ride offer was found
    Given "Alice" searched for a ride offer on "2026-05-01" and found no suitable result
    And a planned departure time "2026-05-01 09:00"
    When "Alice" submits a scheduled ride request as a fallback
    Then a ride request is created with status "Requested"
    And the ride is queued for matching close to the planned departure time

  Scenario: Individual user with a driver profile acts as a passenger
    Given the Individual user "Alice" has an active driver profile
    And "Alice"'s driver availability is deactivated
    And "Alice" has no ongoing ride as a driver
    When "Alice" submits an immediate ride request as a passenger
    Then a ride request is created for "Alice" in the passenger role
    And "Alice"'s driver profile is not affected

  Scenario: Individual user with active driver availability cannot submit a ride request
    Given the Individual user "Alice" has driver availability currently active
    When "Alice" attempts to submit a ride request as a passenger
    Then the request is rejected
    And an error "Please deactivate your driver availability before requesting a ride as a passenger" is displayed

  Scenario: Professional account cannot submit a ride request
    Given the Professional driver "Jean" is logged in
    When "Jean" attempts to submit a ride request as a passenger
    Then the request is rejected
    And an error "Professional accounts cannot act as a passenger" is displayed

  Scenario: Ride request rejected for suspended account
    Given the user "Bob" has an active restriction of type "Suspension"
    When "Bob" attempts to submit a ride request
    Then the request is rejected
    And an error message "Account suspended, you cannot request a ride" is displayed

  Scenario: Ride request with ride options
    Given "Alice" selects the ride option "child seat"
    And "Alice" selects the ride option "wheelchair accessible"
    When "Alice" submits the ride request
    Then the request includes the options "child seat" and "wheelchair accessible"
    And only drivers with a compatible vehicle profile are eligible for matching

  Scenario: Fare estimate before confirmation
    When "Alice" requests a fare estimate for the trip
    Then a fare estimate is returned including
      | Component            | Present |
      | Base fare            | yes     |
      | Estimated duration   | yes     |
      | Estimated distance   | yes     |
      | Traffic conditions   | yes     |
      | Territorial rules    | yes     |


Feature: Ride Offer Publication
  As a user acting as a driver
  I want to publish a ride offer
  So that passengers can find and join my planned trip

  Background:
    Given the user "Jean" has an active driver profile with a valid vehicle profile
    And "Jean" has no active restriction
    And "Jean" does not have active driver availability on an ongoing ride

  Scenario: Successfully publishing a ride offer
    When "Jean" publishes a ride offer with the following details
      | Field           | Value              |
      | Departure point | Lyon Part-Dieu     |
      | Destination     | Paris Gare de Lyon |
      | Departure date  | 2026-05-10         |
      | Departure time  | 08:00              |
      | Available seats | 3                  |
      | Ride options    | oversized luggage  |
    Then the pricing system automatically calculates the price per seat based on route, territorial rules, and current conditions
    And the calculated price per seat is displayed to "Jean" for information only
    And the ride offer is created with status "Published"
    And it becomes visible in Ride Searches for compatible passengers

  Scenario: Driver cannot publish a ride offer while acting as a passenger
    Given "Jean" currently has an active ride as a passenger in status "In Progress"
    When "Jean" attempts to publish a ride offer
    Then the publication is rejected
    And an error "You cannot act as a driver while you have an active ride as a passenger" is displayed

  Scenario: Driver cannot publish a ride offer while driver availability is active
    Given "Jean" is currently available as a driver (availability is active)
    When "Jean" attempts to publish a ride offer
    Then the publication is rejected
    And an error "Please deactivate your availability before publishing a scheduled ride offer" is displayed

  Scenario: Professional account cannot publish a ride offer
    Given the Professional driver "Marc" is logged in
    When "Marc" attempts to publish a ride offer
    Then the publication is rejected
    And an error "Ride Offers are only available to Individual accounts" is displayed

  Scenario: Professional account cannot respond to a scheduled ride request
    Given the Professional driver "Marc" is available
    When a Scheduled Ride request enters the matching engine
    Then "Marc" is not included in the eligible driver pool
    And only Individual drivers are considered

  Scenario: Ride offer cancelled by the driver before any passenger joins
    Given "Jean" has a ride offer in status "Published" with no passengers
    When "Jean" cancels the ride offer
    Then the ride offer moves to status "Cancelled"
    And no penalty is applied

  Scenario: Ride offer cancelled after passengers have joined
    Given "Jean" has a ride offer in status "Accepted" with 2 passengers
    When "Jean" cancels the ride offer
    Then the ride offer moves to status "Cancelled"
    And all passengers are notified "Your driver has cancelled the ride offer"
    And all passengers receive a full refund
    And a penalty is recorded against "Jean" for the cancellation

  Scenario: Ride offer becomes full
    Given "Jean" has a ride offer with 1 available seat remaining
    And the passenger "Alice" joins the ride offer
    When the last seat is taken
    Then the ride offer status changes to "Full"
    And the ride offer no longer appears in Ride Searches
    And "Jean" is notified that the ride offer is now full

  Scenario: Ride offer expires without any passenger joining
    Given "Jean" has a ride offer in status "Published"
    And the departure time "2026-05-10 08:00" has passed
    And no passenger joined the ride
    When the system checks for expired ride offers
    Then the ride offer moves to status "Cancelled"
    And "Jean" is notified "Your ride offer has expired with no passengers"


Feature: Ride Offer Discovery and Joining
  As a user acting as a passenger
  I want to search for and join existing ride offers
  So that I can travel without submitting a new ride request

  Background:
    Given the user "Alice" is logged in with an active account
    And "Alice" has no active restriction

  Scenario: Passenger finds a matching ride offer
    Given the following ride offers are published
      | Driver  | Departure        | Destination         | Date       | Time  | Seats | Price (system-calculated) |
      | Jean    | Lyon Part-Dieu   | Paris Gare de Lyon  | 2026-05-10 | 08:00 | 3     | 25.00€                    |
      | Paul    | Lyon Perrache    | Paris Bercy         | 2026-05-10 | 09:30 | 2     | 22.00€                    |
    When "Alice" searches for a ride from "Lyon" to "Paris" on "2026-05-10"
    Then both ride offers are returned as results
    And each result displays the driver's reputation score, the system-calculated price per seat, and estimated duration

  Scenario: Passenger joins a ride offer
    Given the ride offer by "Jean" is in status "Published" with 3 available seats
    When "Alice" selects "Jean"'s ride offer and confirms her seat
    Then "Alice" is sent a ride proposal to confirm the booking
    And when "Alice" accepts, her seat is assigned
    And the ride offer's available seats decrease by 1
    And "Jean" is notified that "Alice" has joined the ride

  Scenario: Passenger's joining request declined by the driver
    Given "Jean"'s ride offer allows the driver to approve passengers manually
    When "Alice" requests to join "Jean"'s ride offer
    Then a ride proposal is sent to "Jean" for approval
    And if "Jean" declines, "Alice" is notified "The driver did not accept your request"
    And "Alice"'s seat is not reserved

  Scenario: No matching ride offer found — ride request submitted as fallback
    Given no published ride offer matches "Alice"'s search criteria
    When "Alice" chooses to submit a ride request instead
    Then a ride request is created with status "Requested"
    And the matching engine is notified to find an available driver

  Scenario: Passenger cannot join a full ride offer
    Given "Jean"'s ride offer is in status "Full"
    When "Alice" attempts to join the ride offer
    Then the request is rejected
    And a message "This ride offer is full" is displayed
    And "Alice" is offered similar available ride offers or the option to submit a ride request

  Scenario: Passenger cancels their seat on a ride offer
    Given "Alice" has a confirmed seat on "Jean"'s ride offer in status "Accepted"
    When "Alice" cancels her seat
    Then "Alice"'s assignment is removed
    And the available seat count on the ride offer is incremented by 1
    And the ride offer returns to status "Published" if it was previously "Full"
    And a cancellation fee may apply depending on territorial rules
    And "Jean" is notified of the cancellation


Feature: Driver Responding to Open Ride Requests
  As a user acting as a driver
  I want to browse and respond to open ride requests
  So that I can pick up passengers without waiting for automatic matching

  Background:
    Given the user "Jean" has an active driver profile
    And "Jean" has no active restriction
    And "Jean"'s availability is active

  Scenario: Driver browses open ride requests
    Given the following ride requests are open in "Jean"'s activity zone
      | Passenger | Pickup Point             | Destination         | Departure          |
      | Alice     | 10 rue de la Paix, Paris | Gare du Nord        | 2026-05-10 09:00   |
      | Bob       | Place de la République   | Montparnasse        | 2026-05-10 09:30   |
    When "Jean" browses open ride requests in their activity zone
    Then both ride requests are visible with their pickup point, destination, and estimated earnings

  Scenario: Driver responds to an open ride request
    Given the ride request from "Alice" is open with status "Requested"
    When "Jean" selects "Alice"'s ride request and offers to fulfil it
    Then a ride proposal is sent to "Alice" with "Jean"'s profile details
    And "Alice" can accept or decline the proposal

  Scenario: Passenger accepts the driver's response
    Given "Jean" has responded to "Alice"'s ride request
    And "Alice" receives the ride proposal
    When "Alice" accepts the proposal
    Then the ride is assigned to "Jean"
    And the ride moves to status "Accepted"
    And both parties are notified

  Scenario: Passenger declines the driver's response
    Given "Jean" has responded to "Alice"'s ride request
    When "Alice" declines the proposal
    Then the ride request returns to status "Requested"
    And "Jean" is notified that the passenger declined
    And the automatic matching engine may also process the request in parallel


Feature: Ride Lifecycle
  As the ride management system
  I want to manage ride status transitions
  So that the complete trip is fully traceable
  # This lifecycle applies to rides originating from both the Ride Request flow
  # and the Ride Offer flow (once a passenger joins and the ride is confirmed).

  Scenario: Driver accepts the ride (Ride Request flow)
    Given a ride in status "Proposed" assigned to driver "Jean"
    When driver "Jean" accepts the ride
    Then the ride moves to status "Accepted"
    And the passenger receives a notification with the driver's details
    And the driver's real-time position is shared with the passenger

  Scenario: Passenger joins and confirms a ride offer (Ride Offer flow)
    Given a ride offer by "Jean" in status "Published"
    And passenger "Alice" has accepted the ride proposal for her seat
    Then the ride moves to status "Accepted"
    And "Alice" receives a confirmation with "Jean"'s details and pickup instructions

  Scenario: Passenger pickup
    Given a ride in status "Accepted"
    And the driver has arrived at the pickup point
    When the driver confirms the passenger has boarded
    Then the ride moves to status "Picked Up"
    And the trip timer starts

  Scenario: Ride starts
    Given a ride in status "Picked Up"
    When the driver starts the ride
    Then the ride moves to status "In Progress"
    And GPS tracking of the ride is activated

  Scenario: Arrival at destination
    Given a ride in status "In Progress"
    When the driver confirms arrival at the destination
    Then the ride moves to status "Arrived"
    And the final price calculation is triggered

  Scenario: Ride finalized
    Given a ride in status "Arrived"
    And payment has been successfully processed
    When the system finalizes the ride
    Then the ride moves to status "Finalized"
    And both parties are invited to rate each other
    And the driver is set back to available

  Scenario: Cancellation by the passenger before pickup
    Given a ride in status "Accepted"
    When the passenger "Alice" cancels the ride
    Then the ride moves to status "Cancelled"
    And a cancellation fee may apply depending on territorial rules
    And the driver is notified of the cancellation
    And the driver is set back to available

  Scenario: Cancellation by the driver (Withdrawal)
    Given a ride in status "Accepted"
    When the driver "Jean" cancels the ride
    Then the ride moves to status "Cancelled"
    And the passenger is notified and offered a new driver search
    And the withdrawal is recorded in the driver's history

  Scenario: Incident reported during the ride
    Given a ride in status "In Progress"
    When the driver or the passenger reports an incident
    Then the ride moves to status "Incident"
    And the support team is notified
    And the ride is suspended pending resolution

  Scenario: Invalid state transition is rejected
    Given a ride in status "Finalized"
    When a picked-up transition is attempted
    Then the transition is rejected
    And an error "Invalid transition rule: Finalized -> Picked Up" is raised
