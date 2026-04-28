# Bounded Context: GENERIC DOMAIN — Geolocation & Routing
# Real-time position tracking, route calculation, ETA, and traffic conditions

Feature: Real-Time Position Tracking
  As the platform
  I want to track drivers' real-time positions
  So that matching is optimized and passengers are informed

  Scenario: Driver position update
    Given the driver "Jean" is available
    When their GPS position is updated with coordinates "48.8566, 2.3522"
    Then the new real-time position is stored in the system
    And the position is available to the matching engine

  Scenario: Position shared with the passenger during a ride
    Given a ride in status "Accepted"
    And the driver "Jean" is heading toward the pickup point
    When the driver's position is updated
    Then the real-time position is visible to the passenger "Alice" on the map
    And the ETA to the pickup point is dynamically updated

  Scenario: Position sharing stopped after the ride ends
    Given a ride has just moved to status "Finalized"
    When the finalization is confirmed
    Then the driver's position sharing is stopped for this passenger
    And the position is no longer accessible by the passenger "Alice"


Feature: Route Calculation and ETA
  As the geolocation system
  I want to calculate routes and estimated times of arrival
  So that users are informed and operations are optimized

  Scenario: ETA calculation from driver to passenger
    Given the driver's real-time position is "48.8566, 2.3522"
    And the passenger's position is "48.8600, 2.3300"
    When the system calculates the ETA
    Then an estimated arrival time in minutes is returned
    And this ETA accounts for current traffic conditions

  Scenario: Route calculation for a ride
    Given the pickup point is "10 rue de la Paix, Paris"
    And the destination is "Gare du Nord, Paris"
    When the system calculates the route
    Then an optimized route is returned
    And the distance in kilometers is calculated
    And the estimated duration accounts for real-time traffic conditions

  Scenario: Route recalculation during the ride
    Given a ride is in status "In Progress"
    And the driver deviates from the planned route
    When the system detects the deviation
    Then the route is automatically recalculated
    And the updated ETA is communicated to the passenger

  Scenario: Traffic conditions impact the fare estimate
    Given traffic conditions indicate heavy congestion on the route
    And the estimated duration increases from "15 min" to "25 min"
    When the system updates the estimate
    Then the duration component in the fare estimate is revised upward
    And the passenger is informed of the updated estimate

  Scenario: Coverage zone check — covered area
    Given a passenger in "Lyon" submits a ride request
    And "Lyon" is a zone covered by the platform
    When the system checks coverage
    Then the request is accepted and matching starts

  Scenario: Coverage zone check — uncovered area
    Given a passenger in "Ville-sur-Tourbe" submits a ride request
    And "Ville-sur-Tourbe" is not a covered zone
    When the system checks coverage
    Then the request is rejected
    And a message "Service not available in your area" is displayed
