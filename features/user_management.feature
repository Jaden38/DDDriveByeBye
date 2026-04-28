# Bounded Context: SUPPORTING DOMAIN — User Management
# Profiles, vehicle profiles, availability, and preferences
# Two account types:
#   - Individual: carpooling, dual-role (passenger + optional driver)
#   - Professional: VTC, driver only

Feature: Individual Account Registration
  As a person who wants to use the platform for carpooling
  I want to create an Individual account
  So that I can travel as a passenger and optionally offer rides as a driver

  Scenario: Registering an Individual account
    When the user "Alice Martin" registers as an Individual
    With the following details
      | Field              | Value              |
      | Full name          | Alice Martin       |
      | Email              | alice@example.com  |
      | Phone number       | +33 6 12 34 56 78  |
      | Identity document  | Provided           |
    Then the Individual account is created
    And "Alice" can immediately act as a Passenger
    And "Alice" cannot act as a Driver until a driver profile is added and validated

  Scenario: Individual user adds a driver profile
    Given "Alice" has an active Individual account
    When she submits a driver profile application with
      | Information           | Value                       |
      | Driver's license      | Number XX-123456            |
      | Vehicle insurance     | Company ABC, N° 987654      |
      | Vehicle registration  | Renault Zoe 2023, AA-123-BB |
    Then the application is submitted for verification
    And "Alice"'s driver status is "Pending Validation"
    And her passenger access remains unaffected during validation

  Scenario: Individual driver profile validated
    Given "Alice" has a driver profile in status "Pending Validation"
    When the validation team approves the application
    Then "Alice"'s driver status changes to "Active"
    And she can now activate her availability and act as a Driver
    And she retains full Passenger access

  Scenario: Individual driver profile rejected
    Given "Alice" has a driver profile in status "Pending Validation"
    When the validation team rejects the application with reason "Insurance document expired"
    Then "Alice" is notified with the rejection reason
    And she can resubmit with corrected documents
    And her Passenger access remains unaffected


Feature: Professional Account Registration
  As a professional transport operator
  I want to create a Professional account
  So that I can offer on-demand VTC rides on the platform

  Scenario: Registering a Professional account
    When the user "Jean Dupont" registers as a Professional driver
    With the following details
      | Information           | Value                       |
      | Full name             | Jean Dupont                 |
      | Email                 | jean@vtc.com                |
      | Phone number          | +33 6 98 76 54 32           |
      | VTC license           | Number VTC-789012           |
      | Driver's license      | Number XX-654321            |
      | Vehicle insurance     | Professional policy N° 1122 |
      | Vehicle registration  | Mercedes Classe E, BB-456-CC|
      | Identity document     | Provided                    |
    Then the application is submitted for verification
    And "Jean"'s Professional account status is "Pending Validation"
    And "Jean" cannot act as a Passenger at any point

  Scenario: Professional account validated
    Given "Jean" has a Professional account in status "Pending Validation"
    When the validation team approves the application
    Then "Jean"'s account status changes to "Active"
    And "Jean" can activate availability and accept Immediate Ride requests
    And an approval notification is sent to "Jean"

  Scenario: Professional account cannot access passenger features
    Given "Jean" has an active Professional account
    When "Jean" attempts to submit a ride request as a passenger
    Then the action is rejected
    And an error "Professional accounts cannot act as a passenger" is displayed

  Scenario: Professional account cannot publish or join Ride Offers
    Given "Jean" has an active Professional account
    When "Jean" attempts to publish a Ride Offer
    Then the action is rejected
    And an error "Ride Offers are only available to Individual accounts" is displayed

  Scenario: Professional account cannot handle Scheduled Rides
    Given "Jean" has an active Professional account
    When a Scheduled Ride request is processed by the matching engine
    Then "Jean" is excluded from the eligible driver pool


Feature: Vehicle Profile Management
  As a driver (Individual or Professional)
  I want to manage my vehicle profile
  So that passengers know what to expect and the matching engine can filter correctly

  Scenario: Registering a vehicle profile
    Given a driver has an active account (Individual or Professional)
    When they register a vehicle with the following characteristics
      | Field              | Value       |
      | Make               | Renault     |
      | Model              | Zoe         |
      | Year               | 2023        |
      | License plate      | AA-123-BB   |
      | Number of seats    | 4           |
      | Fuel type          | Electric    |
      | Available options  | child seat  |
    Then the vehicle profile is added to the driver's profile
    And the vehicle is available for matching on compatible rides

  Scenario: Updating vehicle insurance expiry date
    Given a driver has a registered vehicle profile
    When they update the insurance expiry date
    Then the new information is saved
    And a validity check is triggered if the new date is within 30 days


Feature: Driver Availability Management
  As a driver (Individual or Professional)
  I want to manage my availability
  So that I can indicate when I am ready to accept rides

  Scenario: Individual driver activates availability
    Given the Individual driver "Alice" is active with a valid vehicle profile
    And "Alice" is not currently acting as a passenger on an ongoing ride
    When "Alice" activates her availability
    Then her status changes to "Available"
    And her real-time GPS position is shared with the platform
    And she becomes eligible for Scheduled Ride matching

  Scenario: Professional driver activates availability
    Given the Professional driver "Jean" is active with a valid vehicle profile
    And "Jean" has a defined Working Zone
    When "Jean" activates his availability
    Then his status changes to "Available"
    And his real-time GPS position is shared with the platform
    And he becomes eligible for Immediate Ride matching within his Working Zone only

  Scenario: Driver deactivates availability
    Given a driver is available and has no ongoing ride
    When they deactivate their availability
    Then their status changes to "Offline"
    And they are no longer eligible for matching
    And their GPS position is no longer shared

  Scenario: Automatic unavailability during a ride
    Given a driver has a ride in status "In Progress"
    When the matching engine searches for drivers
    Then the driver is not included in the results
    And they automatically become available again after the ride is finalized

  Scenario: Individual driver cannot activate availability while acting as a passenger
    Given the Individual user "Alice" has an active ride as a passenger in status "In Progress"
    When "Alice" attempts to activate driver availability
    Then the activation is rejected
    And an error "You cannot activate driver availability while you have an active ride as a passenger" is displayed

  Scenario: Individual driver sets a preferred Activity Zone (soft filter)
    Given the Individual driver "Alice" is on her preferences page
    When she defines her Activity Zone as "15th - 16th arrondissement, Paris"
    Then rides outside this zone are deprioritized in matching
    And "Alice" can still receive and accept ride proposals outside the zone

  Scenario: Professional driver defines a Working Zone (hard constraint)
    Given the Professional driver "Jean" is on his profile page
    When he defines his Working Zone as "La Défense - Neuilly-sur-Seine"
    Then "Jean" only receives Immediate Ride proposals within this Working Zone
    And ride requests outside the Working Zone are never sent to "Jean"

  Scenario: Professional driver cannot activate availability without a Working Zone
    Given the Professional driver "Jean" has no Working Zone defined
    When "Jean" attempts to activate his availability
    Then the activation is rejected
    And an error "You must define a Working Zone before activating your availability" is displayed


Feature: Preferences and Ride Options
  As a user
  I want to configure my preferences
  So that I have a personalized experience

  Scenario: Configuring passenger preferences (Individual only)
    Given the Individual user "Alice" is on her preferences page
    When she configures her passenger preferences
      | Preference    | Value           |
      | Noise level   | Quiet           |
      | Temperature   | Cool            |
      | Music         | No music        |
      | Conversation  | No conversation |
    Then the preferences are saved to her profile
    And these preferences are communicated to the driver at the start of a ride

  Scenario: Configuring driver preferences for an Individual driver
    Given the Individual driver "Alice" is on their preferences page
    When they configure their driver preferences
      | Preference                  | Value  |
      | Accept pets                 | No     |
      | Maximum ride duration       | 45 min |
      | Preferred zone              | Paris  |
      | Accept carpooling groupings | Yes    |
    Then the preferences are saved
    And the matching engine takes these preferences into account during assignment

  Scenario: Configuring driver preferences for a Professional driver
    Given the Professional driver "Jean" is on their preferences page
    When they configure their driver preferences
      | Preference             | Value  |
      | Accept pets            | No     |
      | Maximum ride duration  | 60 min |
      | Preferred zone         | Paris  |
    Then the preferences are saved
    And "Accept carpooling groupings" is not available for Professional accounts

  Scenario: Ride options selected by the passenger
    Given the passenger selects the following ride options when submitting a request
      | Option                 | Selected |
      | Child seat             | Yes      |
      | Wheelchair accessible  | Yes      |
      | Oversized luggage      | No       |
    When the request is submitted
    Then only drivers with a vehicle profile supporting these options are eligible
    And the selected options are included in the ride proposal sent to the driver
