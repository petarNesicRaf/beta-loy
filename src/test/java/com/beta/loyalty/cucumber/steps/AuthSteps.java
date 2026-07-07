package com.beta.loyalty.cucumber.steps;

import com.beta.loyalty.cucumber.ScenarioContext;
import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.Role;
import com.beta.loyalty.domain.StaffUser;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.enums.CustomerStatus;
import com.beta.loyalty.domain.enums.StaffStatus;
import com.beta.loyalty.dto.auth.LoginRequest;
import com.beta.loyalty.dto.auth.TokenResponse;
import com.beta.loyalty.repository.auth.CustomerAuthRepository;
import com.beta.loyalty.domain.RefreshToken;
import com.beta.loyalty.repository.auth.CustomerIdentityRepository;
import com.beta.loyalty.repository.auth.StaffAuthRepository;
import com.beta.loyalty.security.UserType;
import com.beta.loyalty.security.google.GoogleIdTokenVerifierService;
import com.beta.loyalty.security.jwt.JwtProperties;
import com.beta.loyalty.security.jwt.JwtService;
import com.beta.loyalty.service.auth.AuthService;
import com.beta.loyalty.service.auth.RefreshTokenService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthSteps {

    private CustomerAuthRepository customerRepo;
    private StaffAuthRepository staffRepo;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private JwtProperties jwtProps;
    private CustomerIdentityRepository customerIdentityRepo;
    private GoogleIdTokenVerifierService googleVerifier;
    private RefreshTokenService refreshTokenService;
    private AuthService authService;

    private TokenResponse tokenResponse;

    @Before
    public void setUp() {
        customerRepo = mock(CustomerAuthRepository.class);
        staffRepo = mock(StaffAuthRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        jwtProps = mock(JwtProperties.class);
        customerIdentityRepo = mock(CustomerIdentityRepository.class);
        googleVerifier = mock(GoogleIdTokenVerifierService.class);
        refreshTokenService = mock(RefreshTokenService.class);

        when(jwtProps.accessTokenSeconds()).thenReturn(3600L);
        when(jwtProps.refreshTokenSeconds()).thenReturn(2592000L);
        when(jwtService.mintAccessToken(any(), any())).thenReturn("mock-jwt-token");

        RefreshToken mockRt = new RefreshToken();
        mockRt.setToken(UUID.randomUUID());
        when(refreshTokenService.create(any(), any(UserType.class), any(), any())).thenReturn(mockRt);

        authService = new AuthService(customerRepo, staffRepo, passwordEncoder,
                jwtService, jwtProps, customerIdentityRepo, googleVerifier, refreshTokenService);

        tokenResponse = null;
    }

    @Given("a customer exists with email {string} and a valid password")
    public void aCustomerExistsWithEmailAndValidPassword(String email) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setPasswordHash("hashed-secret");

        when(customerRepo.findByEmailIgnoreCase(email)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);
        when(passwordEncoder.matches(eq("secret"), eq("hashed-secret"))).thenReturn(true);
    }

    @Given("a disabled customer exists with email {string}")
    public void aDisabledCustomerExistsWithEmail(String email) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setStatus(CustomerStatus.DISABLED);
        customer.setPasswordHash("hashed-secret");

        when(customerRepo.findByEmailIgnoreCase(email)).thenReturn(Optional.of(customer));
    }

    @When("the customer logs in with email {string} and password {string}")
    public void theCustomerLogsInWithEmailAndPassword(String email, String password) {
        try {
            tokenResponse = authService.customerLogin(new LoginRequest(email, password));
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @Then("a JWT token is returned for the customer")
    public void aJwtTokenIsReturnedForCustomer() {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isEqualTo("mock-jwt-token");
        assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");
    }

    @Given("a staff user exists with email {string} and a valid password")
    public void aStaffUserExistsWithEmailAndValidPassword(String email) {
        Role role = mock(Role.class);
        when(role.getName()).thenReturn("STAFF");

        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        StaffUser staff = new StaffUser();
        staff.setId(UUID.randomUUID());
        staff.setStatus(StaffStatus.ACTIVE);
        staff.setPasswordHash("hashed-secret");
        staff.setTenant(tenant);
        staff.setRoles(new java.util.HashSet<>(Set.of(role)));

        when(staffRepo.findByEmailIgnoreCase(email)).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);
        when(passwordEncoder.matches(eq("secret"), eq("hashed-secret"))).thenReturn(true);
    }

    @When("the staff user logs in with email {string} and password {string}")
    public void theStaffUserLogsInWithEmailAndPassword(String email, String password) {
        try {
            tokenResponse = authService.staffLogin(new LoginRequest(email, password));
        } catch (Exception e) {
            ScenarioContext.setException(e);
        }
    }

    @Then("a JWT token is returned for the staff user")
    public void aJwtTokenIsReturnedForStaffUser() {
        assertThat(ScenarioContext.getException()).isNull();
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isEqualTo("mock-jwt-token");
    }
}