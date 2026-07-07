Feature: Receipt Claiming and Points Award

  Background:
    Given an authenticated customer with id "11111111-1111-1111-1111-111111111111"

  Scenario: Valid receipt claim earns points
    Given a venue with PIB "123456789"
    And no existing receipt in the repository
    When the customer claims a receipt with PIB "123456789" amount 1500.00 currency "RSD"
    Then the claim is finalized with 15 points earned

  Scenario: Receipt claim is idempotent when already finalized
    Given a venue with PIB "123456789"
    And an existing finalized claim in the repository
    When the customer claims a receipt with PIB "123456789" amount 1500.00 currency "RSD"
    Then the claim is finalized with 0 points earned

  Scenario: Receipt claim fails when PIB does not match venue
    Given a venue with PIB "123456789"
    When the customer claims a receipt with PIB "999999999" amount 1000.00 currency "RSD"
    Then an IllegalArgumentException is thrown with message "Receipt PIB does not match venue"

  Scenario Outline: Points calculation follows floor(amount / 100) rule
    Given a venue with PIB "123456789"
    And no existing receipt in the repository
    When the customer claims a receipt with PIB "123456789" amount <amount> currency "RSD"
    Then the claim is finalized with <points> points earned

    Examples:
      | amount  | points |
      | 99.99   | 0      |
      | 100.00  | 1      |
      | 1500.00 | 15     |
      | 9999.99 | 99     |