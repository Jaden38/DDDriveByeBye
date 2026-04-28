# Bounded Context: SUPPORTING DOMAIN — Reputation
# Mutual rating system, penalties, and restrictions

Feature: Mutual Rating After a Ride
  As a platform user
  I want to rate my experience after a ride
  So that I can contribute to overall service quality

  Background:
    Given a ride in status "Finalized"
    And the passenger "Alice" and the driver "Jean" are involved

  Scenario: Passenger rates the driver
    When the passenger "Alice" gives a rating of 5 stars to the driver "Jean"
    And adds a comment "Very professional and punctual driver"
    Then the rating is recorded
    And the driver "Jean"'s reputation score is updated
    And the rating is visible on the driver's public profile

  Scenario: Driver rates the passenger
    When the driver "Jean" gives a rating of 4 stars to the passenger "Alice"
    Then the rating is recorded
    And the passenger "Alice"'s reputation score is updated

  Scenario: Rating not submitted within the allowed window
    Given 48 hours have elapsed since the ride was finalized
    When the passenger "Alice" has not submitted a rating
    Then the rating window is closed
    And no rating is recorded for this ride

  Scenario: Rating submitted with an issue report
    When the passenger "Alice" gives a rating of 1 star
    And selects the reason "Inappropriate behavior"
    Then the rating is recorded
    And a report is created and forwarded to the moderation team
    And the rating is factored into the reputation score calculation

  Scenario: Reputation score calculation
    Given the driver "Jean" has the following 10 most recent ratings
      | Rating |
      | 5      |
      | 4      |
      | 5      |
      | 3      |
      | 5      |
      | 4      |
      | 5      |
      | 4      |
      | 5      |
      | 5      |
    When the system recalculates the reputation score
    Then the reputation score is "4.5"
    And this score is displayed on the driver's public profile


Feature: Penalties and Restrictions
  As the reputation system
  I want to apply penalties and restrictions to problematic users
  So that platform quality and safety are maintained

  Scenario: Warning issued for score below the reputation threshold
    Given the driver "Pierre" has a reputation score of "3.2"
    And the platform warning threshold is "3.5"
    When the system checks reputation thresholds
    Then a warning is sent to the driver "Pierre"
    And a message "Your rating is below the required threshold. Declined rides or poor ratings may result in suspension" is displayed

  Scenario: Automatic suspension for critical score
    Given the driver "Pierre" has a reputation score of "2.8"
    And the platform suspension threshold is "3.0"
    When the system checks reputation thresholds
    Then a restriction of type "Suspension" is applied to the driver "Pierre"
    And the driver is notified of the suspension and its duration
    And the driver can no longer accept new rides

  Scenario: Penalty for repeated cancellations by the driver
    Given the driver "Jean" has cancelled 5 rides in the last 7 days
    And the tolerated cancellation threshold is 3 per week
    When the system analyzes weekly cancellations
    Then a penalty is applied to the driver "Jean"
    And the reason "High cancellation rate" is recorded
    And the penalty negatively impacts the driver's reputation score

  Scenario: Restriction applied to passenger for reported behavior
    Given the passenger "Bob" has received 3 reports for inappropriate behavior
    When the moderation team validates the reports
    Then a restriction is applied to the passenger "Bob"
    And the passenger is notified with the reason and duration of the restriction

  Scenario: Restriction lifted after suspension period
    Given the driver "Pierre" has a "Suspension" restriction for 7 days
    And the suspension started on "2026-04-20"
    When the date "2026-04-27" is reached
    Then the restriction is automatically lifted
    And the driver "Pierre" can accept rides again
    And a notification of the lifted restriction is sent to them

  Scenario: Reputation score checked during matching
    Given the driver "Marc" has a reputation score of "2.9"
    And the minimum score required to accept rides is "3.0"
    When the matching engine searches for available drivers
    Then the driver "Marc" is excluded from the matching results
    And only drivers meeting the reputation threshold are proposed
