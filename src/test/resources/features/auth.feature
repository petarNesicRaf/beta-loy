Feature: Customer and Staff Authentication

  Scenario: Customer logs in with correct credentials
    Given a customer exists with email "alice@example.com" and a valid password
    When the customer logs in with email "alice@example.com" and password "secret"
    Then a JWT token is returned for the customer

  Scenario: Customer login fails with wrong password
    Given a customer exists with email "alice@example.com" and a valid password
    When the customer logs in with email "alice@example.com" and password "wrong"
    Then an UnauthorizedException is thrown

  Scenario: Customer login fails when account is disabled
    Given a disabled customer exists with email "blocked@example.com"
    When the customer logs in with email "blocked@example.com" and password "secret"
    Then an UnauthorizedException is thrown

  Scenario: Staff user logs in with correct credentials
    Given a staff user exists with email "staff@acme.com" and a valid password
    When the staff user logs in with email "staff@acme.com" and password "secret"
    Then a JWT token is returned for the staff user