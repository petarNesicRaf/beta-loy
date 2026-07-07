Feature: Points Service

  Scenario: Earning points updates the account balance
    Given a customer has 0 points at a venue
    When 50 points are earned from a receipt claim
    Then the customer account balance is 50

  Scenario: Debiting points reduces the balance
    Given a customer has 100 points at a venue
    When 30 points are debited for a redemption
    Then the customer account balance is 70

  Scenario: Debit fails when points are insufficient
    Given a customer has 10 points at a venue
    When 50 points are debited for a redemption
    Then an IllegalStateException is thrown with message "Insufficient points"

  Scenario: Earning points is idempotent for the same source
    Given a customer has 0 points at a venue
    And a ledger entry already exists for the same receipt claim
    When 50 points are earned from the same receipt claim
    Then no additional points are awarded