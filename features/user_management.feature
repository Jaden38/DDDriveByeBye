# Bounded Context: SUPPORTING DOMAIN — User Management
# Profiles, vehicle profiles, availability, and preferences

Feature: Driver Profile Management
  As a driver
  I want to manage my profile and vehicle information
  So that I can be available for rides on the platform

  Scenario: New driver registration
    Given the user "Jean Dupont" wants to become a driver
    When they submit their registration with
      | Information             | Value                        |
      | Driver's license        | Number XX-123456             |
      | Vehicle insurance       | Company ABC, N° 987654       |
      | Vehicle registration    | Renault Zoe 2023, AA-123-BB  |
      | Proof of address        | Provided                     |
      | Identity document       | Provided                     |
    Then the application is submitted for verification
    And the driver's status is "Pending Validation"
    And a confirmation notification is sent

  Scenario: Driver application validated
    Given the driver "Jean Dupont" has status "Pending Validation"
    When the validation team approves the application
    Then the driver's status changes to "Active"
    And the driver can now activate their availability
    And an approval notification is sent to them

  Scenario: Registering a vehicle profile
    Given the driver "Jean" is active
    When they register a vehicle with the following characteristics
      | Field                | Value        |
      | Make                 | Renault      |
      | Model                | Zoe          |
      | Year                 | 2023         |
      | License plate        | AA-123-BB    |
      | Number of seats      | 4            |
      | Fuel type            | Electric     |
      | Available options    | child seat   |
    Then the vehicle profile is added to the driver's profile
    And the vehicle is available for matching on compatible rides

  Scenario: Updating vehicle profile information
    Given the driver "Jean" has a registered vehicle profile
    When they update the insurance expiry date
    Then the new information is saved
    And a validity check is triggered if the new date is within 30 days


Feature: Driver Availability Management
  As a driver
  I want to manage my availability
  So that I can indicate when I am ready to accept rides

  Background:
    Given the driver "Jean" is active with a valid vehicle profile

  Scenario: Activating availability
    Given the driver "Jean" is offline
    When they activate their availability
    Then their status changes to "Available"
    And their real-time GPS position is shared with the platform
    And they become eligible for matching

  Scenario: Deactivating availability
    Given the driver "Jean" is available and has no ongoing ride
    When they deactivate their availability
    Then their status changes to "Offline"
    And they are no longer eligible for matching
    And their GPS position is no longer shared

  Scenario: Automatic unavailability during a ride
    Given the driver "Jean" has a ride in status "In Progress"
    When the matching engine searches for drivers
    Then the driver "Jean" is not included in the results
    And they automatically become available again after the ride is finalized

  Scenario: Setting a preferred activity zone
    Given the driver "Jean" is on their preferences page
    When they define their preferred activity zone as "15th - 16th arrondissement, Paris"
    Then rides outside this zone are deprioritized in matching
    And the driver can still manually accept rides outside the zone


Feature: Preferences and Ride Options
  As a user
  I want to configure my ride preferences
  So that I have a personalized experience

  Scenario: Configuring passenger preferences
    Given the passenger "Alice" is on her preferences page
    When she configures her passenger preferences
      | Preference          | Value             |
      | Noise level         | Quiet             |
      | Temperature         | Cool              |
      | Music               | No music          |
      | Conversation        | No conversation   |
    Then the preferences are saved to her profile
    And these preferences are communicated to the driver at the start of a ride

  Scenario: Configuring driver preferences
    Given the driver "Jean" is on their preferences page
    When they configure their driver preferences
      | Preference                   | Value    |
      | Accept pets                  | No       |
      | Maximum ride duration        | 45 min   |
      | Preferred zone               | Paris    |
      | Accept carpooling groupings  | Yes      |
    Then the preferences are saved
    And the matching engine takes these preferences into account during assignment

  Scenario: Ride options selected by the passenger
    Given the passenger selects the following ride options when submitting a request
      | Option                   | Selected |
      | Child seat               | Yes      |
      | Wheelchair accessible    | Yes      |
      | Oversized luggage        | No       |
    When the request is submitted
    Then only drivers with a vehicle profile supporting these options are eligible
    And the selected options are included in the ride proposal sent to the driver
