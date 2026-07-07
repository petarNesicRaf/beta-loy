Feature: Redemption Requests

  Scenario: Customer creates a redemption request for an active reward
    Given an active reward costing 100 points
    And no existing request for idempotency key "idem-001"
    When the customer creates a redemption request with idempotency key "idem-001"
    Then a redemption request with status PENDING is returned

  Scenario: Redemption request creation is idempotent
    Given an existing redemption request with idempotency key "idem-002"
    When the customer creates a redemption request with idempotency key "idem-002"
    Then the existing redemption request is returned with status PENDING

  Scenario: Redemption request fails when reward is not active
    Given an inactive reward
    And no existing request for idempotency key "idem-003"
    When the customer creates a redemption request with idempotency key "idem-003"
    Then an IllegalStateException is thrown with message "Reward not active"

  Scenario: Staff approves a pending redemption request
    Given a PENDING redemption request with 100 points cost
    And the staff member is assigned to the venue
    When the staff member decides to APPROVE the redemption request
    Then the redemption request status is APPROVED
    And points are debited from the customer account

  Scenario: Fulfilling a non-approved request throws an error
    Given a PENDING redemption request with 100 points cost
    And the staff member is assigned to the venue
    When the staff member tries to fulfill the redemption request
    Then an IllegalStateException is thrown with message "Can only fulfill approved request"