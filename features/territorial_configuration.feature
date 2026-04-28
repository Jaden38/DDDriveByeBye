# Bounded Context: GENERIC DOMAIN — Territorial Configuration
# Operational, regulatory, and commercial rules per territory

Feature: Territorial Rules Management
  As a platform administrator
  I want to configure territory-specific rules
  So that local legal and commercial constraints are respected

  Background:
    Given I am logged in as a platform administrator

  Scenario: Creating a new territory
    When I create the territory "Lyon - Métropole" with the following parameters
      | Parameter                    | Value               |
      | Geographic zone              | 50 km radius, Lyon  |
      | Per-kilometer rate           | 1.20€/km            |
      | Per-minute rate              | 0.35€/min           |
      | Pickup fee                   | 2.50€               |
      | Maximum surge coefficient    | 3.0                 |
      | Cancellation fee             | 5.00€               |
      | Carpooling grouping          | Enabled             |
    Then the territory "Lyon - Métropole" is created
    And the territorial rules apply to all rides within this zone

  Scenario: Regulatory constraint enforcement
    Given the territory "Paris - Île-de-France" has the regulatory constraint "VTC license required"
    When a driver without a VTC license attempts to activate in this zone
    Then their activation is blocked
    And a message "VTC license required to operate in this zone" is displayed
    And the driver is prompted to provide their license

  Scenario: Fixed fare for a special zone
    Given the territory "CDG Airport" has a fixed fare configured at "55.00€"
    When a ride is requested with the destination "Terminal 2F - CDG Airport"
    Then the pricing engine applies the fixed fare of "55.00€"
    And the standard dynamic pricing calculation is bypassed

  Scenario: Updating territorial rules
    Given the territory "Lyon - Métropole" exists with a per-km rate of "1.20€"
    When I update the per-kilometer rate to "1.35€"
    Then the update is saved
    And new rides in this territory use the updated rate
    And rides already in progress are not affected

  Scenario: Deactivating a territory
    Given the territory "Pilot Test City" is active
    When I deactivate this territory
    Then no new ride can be requested in this zone
    And rides already in progress are finalized normally
    And drivers in this zone are notified

  Scenario: Conflicting rules between overlapping territories
    Given the territory "Île-de-France" covers Paris
    And the territory "Paris Centre - Zone 1" also covers central Paris
    When a ride is requested in central Paris
    Then the rules of the most specific territory "Paris Centre - Zone 1" apply
    And the general "Île-de-France" rules serve as a fallback for undefined parameters

  Scenario: Territorial configuration queried by the pricing system
    Given the pricing system is calculating the fare for a ride in "Bordeaux"
    When it queries the rules of the territory "Bordeaux - Métropole"
    Then the local pricing parameters are returned
    And the territory-specific regulatory constraints are included
    And the pricing system can apply the correct calculation
