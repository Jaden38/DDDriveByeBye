# Bounded Context: SUPPORTING DOMAIN — Pricing
# Dynamic pricing calculation based on multiple factors

Feature: Dynamic Pricing Calculation
  As the pricing system
  I want to calculate a fare adapted to the ride context
  So that it reflects actual demand and operational costs

  Background:
    Given a ride with the following characteristics
      | Pickup Point | 10 rue de la Paix, Paris |
      | Destination  | Gare du Nord, Paris      |
      | Distance     | 3.2 km                   |
      | Duration     | 12 min                   |
    And the territory is "Paris - Île-de-France"

  Scenario: Standard fare calculation without surge
    Given driver demand is normal (surge coefficient 1.0)
    And traffic conditions are clear
    When the system calculates the fare
    Then the base fare is calculated using the formula "base_fare + (km × per_km_rate) + (min × per_min_rate)"
    And the final price is within the expected range for this territory

  Scenario: Surge applied during high demand
    Given driver demand is high (surge coefficient 2.5)
    And the number of available drivers is below 20% of demand
    When the system calculates the fare
    Then a surge coefficient of 2.5 is applied
    And the passenger is informed of the surge before confirming
    And the final price is "base_fare × 2.5"

  Scenario: Discount applied via promotional code
    Given the passenger uses the promotional offer code "WELCOME20"
    And the code "WELCOME20" grants a 20% discount valid once
    When the system calculates the fare
    Then a 20% discount is applied to the final price
    And the promotional offer code is marked as used for this passenger

  Scenario: Expired promotional offer code
    Given the passenger uses the promotional offer code "SUMMER2025"
    And the code "SUMMER2025" expired on "2025-09-01"
    When the system attempts to apply the code
    Then the discount is not applied
    And a message "Promotional offer expired" is returned to the passenger

  Scenario: Fare adjustment based on territorial rules
    Given the territory is "CDG Airport - Regulated Zone"
    And the territorial rules define a fixed airport fare
    When the system calculates the fare
    Then the fixed territorial fare of "55€" is applied
    And the standard dynamic pricing calculation is bypassed for this zone

  Scenario: Fare impacted by traffic conditions
    Given traffic conditions are heavy (index 0.3)
    And the estimated duration is extended by 40%
    When the system calculates the fare
    Then the duration component of the fare accounts for the revised duration
    And the updated ETA is communicated to the passenger

  Scenario: Different fare for carpooling grouping
    Given the ride is a carpooling ride with 2 grouped passengers
    When the system calculates the fare for each passenger
    Then each passenger pays a reduced fare compared to an individual ride
    And the driver receives total compensation greater than for a solo ride

  Scenario: Final price calculated upon arrival
    Given a ride in status "Arrived"
    And the actual distance is "3.5 km" (slightly different from the estimate "3.2 km")
    And the actual duration is "15 min" (vs. estimate "12 min")
    When the system calculates the final price
    Then the final price is based on the actual distance and duration
    And not on the initial fare estimate
    And the passenger is informed of the final price before closure


Feature: Fare Estimate
  As a passenger
  I want to get a fare estimate before confirming my ride
  So that I can make an informed decision

  Scenario: Standard fare estimate displayed to the passenger
    Given a ride request with pickup point and destination filled in
    When the passenger requests a fare estimate
    Then the estimate displays
      | Field                    | Value             |
      | Minimum estimated fare   | present           |
      | Maximum estimated fare   | present           |
      | Estimated trip duration  | present           |
      | Estimated distance       | present           |
      | Surge coefficient        | present if > 1.0  |

  Scenario: High surge warning shown to passenger
    Given the current surge coefficient is "3.0"
    When the passenger views the fare estimate
    Then a warning "High demand — fares surged ×3.0" is prominently displayed
    And the passenger must explicitly confirm before validating the ride request
