# Bounded Context: CORE DOMAIN — Ride Management
# Manages the complete ride lifecycle and state machine

Feature: Ride Request
  As a passenger
  I want to submit a ride request
  So that I can be matched with an available driver

  Background:
    Given the passenger "Alice" is logged in with an active account
    And the passenger "Alice" has no active restriction
    And a pickup point "10 rue de la Paix, Paris"
    And a destination "Gare du Nord, Paris"

  Scenario: Successful immediate ride request
    When the passenger submits an immediate ride request
    Then a ride request is created with status "Requested"
    And a fare estimate is calculated and displayed to the passenger
    And the matching engine is notified of the new ride request

  Scenario: Scheduled ride request
    Given a planned departure date and time in the future "2026-05-01 09:00"
    When the passenger submits a scheduled ride request
    Then the ride is created with status "Requested"
    And the ride is queued for assignment close to the planned departure time

  Scenario: Ride request rejected for restricted account
    Given the passenger "Bob" has an active restriction of type "Suspension"
    When the passenger "Bob" attempts to submit a ride request
    Then the request is rejected
    And an error message "Account suspended, you cannot request a ride" is displayed

  Scenario: Ride request with ride options
    Given the passenger selects the ride option "child seat"
    And the passenger selects the ride option "wheelchair accessible"
    When the passenger submits the ride request
    Then the request includes the options "child seat" and "wheelchair accessible"
    And only drivers with a compatible vehicle profile are eligible for matching

  Scenario: Fare estimate before confirmation
    When the passenger requests a fare estimate for the trip
    Then a fare estimate is returned including
      | Component            | Present |
      | Base fare            | yes     |
      | Estimated duration   | yes     |
      | Estimated distance   | yes     |
      | Traffic conditions   | yes     |
      | Territorial rules    | yes     |


Feature: Ride Lifecycle
  As the ride management system
  I want to manage ride status transitions
  So that the complete trip is fully traceable

  Scenario: Driver accepts the ride
    Given a ride in status "Proposed" assigned to driver "Jean"
    When the driver "Jean" accepts the ride
    Then the ride moves to status "Accepted"
    And the passenger receives a notification with the driver's details
    And the driver's real-time position is shared with the passenger

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
