package com.beta.loyalty.cucumber.steps;

import com.beta.loyalty.cucumber.ScenarioContext;
import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.Receipt;
import com.beta.loyalty.domain.ReceiptClaim;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.ReceiptClaimStatus;
import com.beta.loyalty.dto.receipt.ReceiptClaimRequest;
import com.beta.loyalty.dto.receipt.ReceiptClaimResponse;
import com.beta.loyalty.repository.auth.CustomerAuthRepository;
import com.beta.loyalty.repository.receipt.ReceiptClaimRepository;
import com.beta.loyalty.repository.receipt.ReceiptRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import com.beta.loyalty.security.AuthPrincipal;
import com.beta.loyalty.security.UserType;
import com.beta.loyalty.service.points.PointsService;
import com.beta.loyalty.service.receipt.ReceiptClaimService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReceiptClaimSteps {

    private ReceiptRepository receiptRepository;
    private ReceiptClaimRepository claimRepository;
    private CustomerAuthRepository customerAuthRepository;
    private VenueRepository venueRepository;
    private PointsService pointsService;
    private ReceiptClaimService receiptClaimService;

    private final UUID venueId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private UUID customerId;
    private ReceiptClaimResponse claimResponse;

    @Before
    public void setUp() {
        receiptRepository = mock(ReceiptRepository.class);
        claimRepository = mock(ReceiptClaimRepository.class);
        customerAuthRepository = mock(CustomerAuthRepository.class);
        venueRepository = mock(VenueRepository.class);
        pointsService = mock(PointsService.class);

        receiptClaimService = new ReceiptClaimService(receiptRepository, claimRepository,
                customerAuthRepository, venueRepository, pointsService);

        customerId = null;
        claimResponse = null;
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Given("an authenticated customer with id {string}")
    public void anAuthenticatedCustomerWithId(String customerIdStr) {
        customerId = UUID.fromString(customerIdStr);
        AuthPrincipal principal = new AuthPrincipal(customerId, UserType.CUSTOMER, null);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerAuthRepository.getReferenceById(customerId)).thenReturn(customer);
    }

    @Given("a venue with PIB {string}")
    public void aVenueWithPib(String pib) {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        Venue venue = new Venue();
        venue.setId(venueId);
        venue.setPib(pib);
        venue.setTenant(tenant);

        when(venueRepository.getReferenceById(venueId)).thenReturn(venue);
    }

    @Given("no existing receipt in the repository")
    public void noExistingReceiptInRepository() {
        Receipt receipt = new Receipt();
        receipt.setId(UUID.randomUUID());

        ReceiptClaim claim = new ReceiptClaim();
        claim.setId(UUID.randomUUID());

        when(receiptRepository.findForUpdate(any(UUID.class), any(String.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(receipt));

        when(claimRepository.findByReceipt_Id(receipt.getId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(claim));
    }

    @Given("an existing finalized claim in the repository")
    public void anExistingFinalizedClaimInRepository() {
        Receipt receipt = new Receipt();
        receipt.setId(UUID.randomUUID());

        ReceiptClaim claim = new ReceiptClaim();
        claim.setId(UUID.randomUUID());
        claim.setStatus(ReceiptClaimStatus.FINALIZED);
        claim.setFinalizedAt(OffsetDateTime.now().minusMinutes(10));

        when(receiptRepository.findForUpdate(any(UUID.class), any(String.class)))
                .thenReturn(Optional.of(receipt));

        when(claimRepository.findByReceipt_Id(receipt.getId()))
                .thenReturn(Optional.of(claim));
    }

    @When("the customer claims a receipt with PIB {string} amount {double} currency {string}")
    public void theCustomerClaimsAReceipt(String pib, double amount, String currency) {
        ReceiptClaimRequest req = new ReceiptClaimRequest(
                venueId, pib, null,
                OffsetDateTime.now().minusMinutes(5),
                BigDecimal.valueOf(amount), currency, null
        );
        try {
            claimResponse = receiptClaimService.claimIndividual(req);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @Then("the claim is finalized with {int} points earned")
    public void theClaimIsFinalizedWithPointsEarned(int points) {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(claimResponse).isNotNull();
        assertThat(claimResponse.status()).isEqualTo("FINALIZED");
        assertThat(claimResponse.pointsEarned()).isEqualTo((long) points);
    }
}