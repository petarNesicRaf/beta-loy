package com.beta.loyalty.cucumber.steps;

import com.beta.loyalty.cucumber.ScenarioContext;
import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.PointsAccount;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.repository.auth.CustomerAuthRepository;
import com.beta.loyalty.repository.points.PointsAccountRepository;
import com.beta.loyalty.repository.points.PointsLedgerEntryRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import com.beta.loyalty.service.points.PointsService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PointsSteps {

    private PointsAccountRepository pointsAccountRepository;
    private PointsLedgerEntryRepository ledgerRepository;
    private CustomerAuthRepository customerAuthRepository;
    private VenueRepository venueRepository;
    private PointsService pointsService;

    private final UUID customerId = UUID.fromString("dddddddd-0000-0000-0000-000000000001");
    private final UUID venueId    = UUID.fromString("dddddddd-0000-0000-0000-000000000002");
    private final UUID claimId    = UUID.fromString("dddddddd-0000-0000-0000-000000000003");
    private final UUID rrId       = UUID.fromString("dddddddd-0000-0000-0000-000000000004");

    private PointsAccount account;
    private Venue venue;

    @Before
    public void setUp() {
        pointsAccountRepository = mock(PointsAccountRepository.class);
        ledgerRepository = mock(PointsLedgerEntryRepository.class);
        customerAuthRepository = mock(CustomerAuthRepository.class);
        venueRepository = mock(VenueRepository.class);

        pointsService = new PointsService(pointsAccountRepository, ledgerRepository,
                customerAuthRepository, venueRepository);

        account = null;
        venue = null;

        // Default: no existing ledger entries (can be overridden per scenario)
        when(ledgerRepository.existsBySourceTypeAndSourceIdAndCustomerId(any(), any(), any()))
                .thenReturn(false);
    }

    @Given("a customer has {int} points at a venue")
    public void aCustomerHasPointsAtAVenue(int balance) {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        venue = new Venue();
        venue.setId(venueId);
        venue.setTenant(tenant);

        Customer customer = new Customer();
        customer.setId(customerId);

        account = new PointsAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setVenue(venue);
        account.setCurrentBalance((long) balance);

        when(customerAuthRepository.getReferenceById(customerId)).thenReturn(customer);
        when(venueRepository.getReferenceById(venueId)).thenReturn(venue);
        when(pointsAccountRepository.findForUpdate(customerId, venueId)).thenReturn(Optional.of(account));
    }

    @Given("a ledger entry already exists for the same receipt claim")
    public void aLedgerEntryAlreadyExistsForTheSameReceiptClaim() {
        when(ledgerRepository.existsBySourceTypeAndSourceIdAndCustomerId(
                LedgerSourceType.RECEIPT_CLAIM, claimId, customerId)).thenReturn(true);
    }

    @When("{int} points are earned from a receipt claim")
    public void pointsAreEarnedFromAReceiptClaim(int points) {
        try {
            pointsService.earn(customerId, venueId, points, LedgerSourceType.RECEIPT_CLAIM, claimId);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @When("{int} points are debited for a redemption")
    public void pointsAreDebitedForARedemption(int points) {
        UUID tenantId = venue.getTenant().getId();
        try {
            pointsService.debitForRedemption(tenantId, customerId, venueId, rrId, points);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @When("{int} points are earned from the same receipt claim")
    public void pointsAreEarnedFromTheSameReceiptClaim(int points) {
        try {
            pointsService.earn(customerId, venueId, points, LedgerSourceType.RECEIPT_CLAIM, claimId);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @Then("the customer account balance is {int}")
    public void theCustomerAccountBalanceIs(int expectedBalance) {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(account.getCurrentBalance()).isEqualTo((long) expectedBalance);
    }

    @Then("no additional points are awarded")
    public void noAdditionalPointsAreAwarded() {
        assertThat(ScenarioContext.getException()).isNull();
        verify(pointsAccountRepository, never()).save(any());
    }
}
