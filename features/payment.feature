# Bounded Context: GENERIC DOMAIN — Payment
# Integration with payment providers (e.g. Stripe)

Feature: Ride Payment Processing
  As the payment system
  I want to process payment at the end of a ride
  So that the driver is compensated and the service fee is collected

  Background:
    Given a ride in status "Arrived"
    And the final price of the ride is "18.50€"
    And the passenger "Alice" has a registered payment method

  Scenario: Successful payment by credit card
    Given the passenger's payment method is a valid credit card
    When the system triggers the payment
    Then the transaction is processed successfully
    And "18.50€" is charged to the passenger
    And the ride moves to status "Finalized"
    And a receipt is sent to the passenger by email

  Scenario: Failed payment — card declined
    Given the passenger's payment method is an expired credit card
    When the system triggers the payment
    Then the transaction fails
    And the passenger is notified "Payment declined — please update your payment method"
    And the ride remains in status "Arrived" pending resolution
    And a 24-hour window is granted to the passenger to regularize

  Scenario: Payment via platform credit wallet
    Given the passenger has "25.00€" in platform credit
    When the system triggers the payment of "18.50€"
    Then "18.50€" is debited from the passenger's platform credit
    And the passenger's remaining balance is "6.50€"
    And the ride is finalized successfully

  Scenario: Mixed payment — insufficient credit
    Given the passenger has "10.00€" in platform credit
    And the passenger has a valid credit card as a secondary payment method
    When the system triggers the payment of "18.50€"
    Then "10.00€" is debited from the platform credit
    And "8.50€" is charged to the credit card
    And the ride is finalized successfully

  Scenario: Refund following cancellation with cancellation fee
    Given a cancelled ride with a cancellation fee of "5.00€"
    And the passenger was pre-authorized for "18.50€"
    When the system processes the cancellation
    Then "5.00€" is charged as the cancellation fee
    And "13.50€" is refunded to the passenger
    And a refund receipt is sent

  Scenario: Full refund following an incident
    Given a ride in status "Incident" classified as driver's fault
    And the passenger was charged "18.50€"
    When support validates the full refund
    Then "18.50€" is refunded to the passenger
    And the driver receives no compensation for this ride


Feature: Payment Method Management
  As a passenger
  I want to manage my payment methods
  So that I can pay for rides without friction

  Scenario: Adding a credit card
    Given the passenger is on their payment methods page
    When the passenger adds a credit card "4242 4242 4242 4242" expiry "12/28" CVV "123"
    Then the card is tokenized via the payment provider
    And only the last 4 digits "4242" are stored in plaintext
    And the card is available as a payment method

  Scenario: Removing a payment method
    Given the passenger has 2 registered payment methods
    When the passenger removes a non-default payment method
    Then the payment method is deleted
    And the other payment method remains active

  Scenario: Cannot remove the last payment method
    Given the passenger has only one registered payment method
    When the passenger attempts to remove it
    Then the removal is blocked
    And a message "You must keep at least one payment method" is displayed
