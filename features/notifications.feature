# Bounded Context: GENERIC DOMAIN — Notification
# Alerts and communications triggered by Ride Management (Open Host Service / Published Language)

Feature: User Notifications
  As the notification system
  I want to inform users of important events
  So that they are kept up to date in real time on the status of their ride

  Scenario: Passenger notified when ride is accepted
    Given a ride has just moved to status "Accepted"
    When the notification system is triggered
    Then the passenger receives a notification containing
      | Information                     | Present |
      | Driver's name                   | yes     |
      | Driver's reputation score       | yes     |
      | Vehicle make and color          | yes     |
      | License plate number            | yes     |
      | Driver ETA to pickup point      | yes     |

  Scenario: Driver notified of a new ride proposal
    Given a ride proposal has just been sent to the driver "Jean"
    When the notification system is triggered
    Then the driver "Jean" receives a notification with
      | Information                      | Present |
      | Pickup point                     | yes     |
      | Destination (approximate)        | yes     |
      | Estimated duration               | yes     |
      | Estimated earnings               | yes     |
    And the driver has 30 seconds to respond

  Scenario: Driver notified of ride cancellation
    Given a ride in status "Accepted" has just been cancelled by the passenger
    When the notification system is triggered
    Then the driver "Jean" receives a notification "Ride cancelled by the passenger"
    And the driver is automatically set back to "Available"

  Scenario: Passenger notified of driver cancellation (Withdrawal)
    Given a ride in status "Accepted" has just been cancelled by the driver
    When the notification system is triggered
    Then the passenger receives a notification "Your driver has cancelled the ride"
    And the notification includes a link to restart the driver search

  Scenario: Rating reminder after finalization
    Given a ride has just moved to status "Finalized"
    When the notification system is triggered
    Then the passenger receives a notification inviting them to rate the driver
    And the driver receives a notification inviting them to rate the passenger
    And these notifications are sent after a 2-minute delay

  Scenario: Account suspension notification
    Given a "Suspension" restriction has just been applied to the driver "Pierre"
    When the notification system is triggered
    Then the driver "Pierre" receives a notification detailing
      | Information                 | Present |
      | Reason for the suspension   | yes     |
      | Duration of the suspension  | yes     |
      | Appeal process              | yes     |

  Scenario: Push notification disabled by the user
    Given the passenger "Alice" has disabled push notifications
    When a notification must be sent to them
    Then the push notification is not sent
    And the notification is available in the in-app notification center
    And an email is sent if the notification is high priority

  Scenario: High-priority notification via multiple channels
    Given the system must notify the passenger of a critical incident
    When the notification is triggered
    Then the notification is sent simultaneously via
      | Channel              | Enabled |
      | Mobile push          | yes     |
      | Email                | yes     |
      | SMS (if configured)  | yes     |
      | In-app               | yes     |
