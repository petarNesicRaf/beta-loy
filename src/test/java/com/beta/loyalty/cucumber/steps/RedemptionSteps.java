package com.beta.loyalty.cucumber.steps;

import com.beta.loyalty.cucumber.ScenarioContext;
import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.RedemptionRequest;
import com.beta.loyalty.domain.Reward;
import com.beta.loyalty.domain.StaffUser;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.DecisionType;
import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.dto.redemption.CreateRedemptionRequestDto;
import com.beta.loyalty.dto.redemption.DecideRedemptionRequest;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.repository.customer.CustomerRepository;
import com.beta.loyalty.repository.redemption.RedemptionRequestRepository;
import com.beta.loyalty.repository.reward.RewardRepository;
import com.beta.loyalty.repository.staff.StaffUserRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import com.beta.loyalty.service.points.PointsService;
import com.beta.loyalty.service.redemption.RedemptionDecisionService;
import com.beta.loyalty.service.redemption.RedemptionFulfillmentService;
import com.beta.loyalty.service.redemption.RedemptionRequestService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedemptionSteps {

    private RewardRepository rewardRepository;
    private RedemptionRequestRepository redemptionRequestRepository;
    private CustomerRepository customerRepository;
    private VenueStaffAssignmentRepository staffAssignmentRepository;
    private StaffUserRepository staffUserRepository;
    private PointsService pointsService;

    private RedemptionRequestService requestService;
    private RedemptionDecisionService decisionService;
    private RedemptionFulfillmentService fulfillmentService;

    private final UUID customerId = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
    private final UUID venueId    = UUID.fromString("cccccccc-0000-0000-0000-000000000002");
    private final UUID rewardId   = UUID.fromString("cccccccc-0000-0000-0000-000000000003");
    private final UUID rrId       = UUID.fromString("cccccccc-0000-0000-0000-000000000004");
    private final UUID staffId    = UUID.fromString("cccccccc-0000-0000-0000-000000000005");

    private RedemptionRequest pendingRr;
    private RedemptionRequestDto resultDto;

    @Before
    public void setUp() {
        rewardRepository = mock(RewardRepository.class);
        redemptionRequestRepository = mock(RedemptionRequestRepository.class);
        customerRepository = mock(CustomerRepository.class);
        staffAssignmentRepository = mock(VenueStaffAssignmentRepository.class);
        staffUserRepository = mock(StaffUserRepository.class);
        pointsService = mock(PointsService.class);

        requestService = new RedemptionRequestService(rewardRepository,
                redemptionRequestRepository, customerRepository);
        decisionService = new RedemptionDecisionService(redemptionRequestRepository,
                staffAssignmentRepository, rewardRepository, staffUserRepository, pointsService);
        fulfillmentService = new RedemptionFulfillmentService(
                redemptionRequestRepository, staffAssignmentRepository);

        pendingRr = null;
        resultDto = null;
    }

    private Venue buildVenue() {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        Venue venue = new Venue();
        venue.setId(venueId);
        venue.setTenant(tenant);
        return venue;
    }

    @Given("an active reward costing {int} points")
    public void anActiveRewardCostingPoints(int cost) {
        Venue venue = buildVenue();

        Reward reward = new Reward();
        reward.setId(rewardId);
        reward.setVenue(venue);
        reward.setPointsCost(cost);
        reward.setStatus(RewardStatus.ACTIVE);

        when(rewardRepository.findByIdAndVenueId(rewardId, venueId)).thenReturn(Optional.of(reward));
        when(customerRepository.getReferenceById(customerId)).thenReturn(mock(Customer.class));
    }

    @Given("an inactive reward")
    public void anInactiveReward() {
        Venue venue = buildVenue();

        Reward reward = new Reward();
        reward.setId(rewardId);
        reward.setVenue(venue);
        reward.setPointsCost(100L);
        reward.setStatus(RewardStatus.INACTIVE);

        when(rewardRepository.findByIdAndVenueId(rewardId, venueId)).thenReturn(Optional.of(reward));
    }

    @Given("no existing request for idempotency key {string}")
    public void noExistingRequestForIdempotencyKey(String idemKey) {
        when(redemptionRequestRepository.findByCustomerIdAndIdempotencyKey(customerId, idemKey))
                .thenReturn(Optional.empty());
    }

    @Given("an existing redemption request with idempotency key {string}")
    public void anExistingRedemptionRequestWithIdempotencyKey(String idemKey) {
        Venue venue = buildVenue();

        Reward reward = new Reward();
        reward.setId(rewardId);
        reward.setVenue(venue);
        reward.setPointsCost(100L);

        Customer customer = new Customer();
        customer.setId(customerId);

        RedemptionRequest existing = new RedemptionRequest();
        existing.setId(rrId);
        existing.setVenue(venue);
        existing.setReward(reward);
        existing.setCustomer(customer);
        existing.setStatus(RedemptionStatus.PENDING);
        existing.setPointsCostSnapshot(100L);
        existing.setRequestedAt(OffsetDateTime.now().minusMinutes(1));
        existing.setExpiresAt(OffsetDateTime.now().plusMinutes(4));

        when(redemptionRequestRepository.findByCustomerIdAndIdempotencyKey(customerId, idemKey))
                .thenReturn(Optional.of(existing));
    }

    @When("the customer creates a redemption request with idempotency key {string}")
    public void theCustomerCreatesARedemptionRequest(String idemKey) {
        CreateRedemptionRequestDto dto = new CreateRedemptionRequestDto(rewardId, idemKey, null);
        try {
            resultDto = requestService.create(customerId, venueId, dto);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @Then("a redemption request with status PENDING is returned")
    public void aRedemptionRequestWithStatusPendingIsReturned() {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.status()).isEqualTo(RedemptionStatus.PENDING);
    }

    @Then("the existing redemption request is returned with status PENDING")
    public void theExistingRedemptionRequestIsReturnedWithStatusPending() {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.status()).isEqualTo(RedemptionStatus.PENDING);
        assertThat(resultDto.id()).isEqualTo(rrId);
    }

    @Given("a PENDING redemption request with {int} points cost")
    public void aPendingRedemptionRequestWithPointsCost(int pointsCost) {
        Venue venue = buildVenue();
        UUID tenantId = venue.getTenant().getId();

        Reward reward = new Reward();
        reward.setId(rewardId);
        reward.setVenue(venue);
        reward.setPointsCost(pointsCost);

        Customer customer = new Customer();
        customer.setId(customerId);

        pendingRr = new RedemptionRequest();
        pendingRr.setId(rrId);
        pendingRr.setTenantId(tenantId);
        pendingRr.setVenue(venue);
        pendingRr.setReward(reward);
        pendingRr.setCustomer(customer);
        pendingRr.setPointsCostSnapshot(pointsCost);
        pendingRr.setStatus(RedemptionStatus.PENDING);
        pendingRr.setRequestedAt(OffsetDateTime.now());
        pendingRr.setExpiresAt(OffsetDateTime.now().plusHours(1));

        when(redemptionRequestRepository.findByIdForUpdate(rrId)).thenReturn(Optional.of(pendingRr));
    }

    @Given("the staff member is assigned to the venue")
    public void theStaffMemberIsAssignedToTheVenue() {
        when(staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffId, venueId))
                .thenReturn(true);
        when(staffUserRepository.getReferenceById(staffId)).thenReturn(mock(StaffUser.class));
    }

    @When("the staff member decides to APPROVE the redemption request")
    public void theStaffMemberApprovesTheRedemptionRequest() {
        DecideRedemptionRequest req = new DecideRedemptionRequest(DecisionType.APPROVE, null);
        try {
            resultDto = decisionService.decide(staffId, rrId, req);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @Then("the redemption request status is APPROVED")
    public void theRedemptionRequestStatusIsApproved() {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.status()).isEqualTo(RedemptionStatus.APPROVED);
    }

    @And("points are debited from the customer account")
    public void pointsAreDebitedFromCustomerAccount() {
        verify(pointsService).debitForRedemption(
                any(UUID.class), eq(customerId), eq(venueId), eq(rrId), anyLong());
    }

    @When("the staff member tries to fulfill the redemption request")
    public void theStaffMemberTriesToFulfillTheRedemptionRequest() {
        try {
            fulfillmentService.fulfill(staffId, rrId);
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }
}
