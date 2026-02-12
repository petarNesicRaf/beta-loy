package com.beta.loyalty.auth.service;

import com.beta.loyalty.auth.AuthPrincipal;
import com.beta.loyalty.auth.UserType;
import com.beta.loyalty.auth.jwt.dto.LoginRequest;
import com.beta.loyalty.auth.TokenResponse;
import com.beta.loyalty.auth.jwt.token.JwtService;
import com.beta.loyalty.auth.jwt.repo.CustomerAuthRepository;
import com.beta.loyalty.auth.jwt.repo.StaffAuthRepository;
import com.beta.loyalty.auth.oauth.OauthProvider;
import com.beta.loyalty.auth.oauth.google.GoogleIdTokenVerifierService;
import com.beta.loyalty.auth.oauth.google.GoogleLoginRequest;
import com.beta.loyalty.auth.oauth.repo.CustomerIdentityRepository;
import com.beta.loyalty.common.exceptions.UnauthorizedException;
import com.beta.loyalty.auth.jwt.config.JwtProperties;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.customer.CustomerIdentity;
import com.beta.loyalty.domain.enums.CustomerStatus;
import com.beta.loyalty.domain.enums.StaffStatus;
import com.beta.loyalty.domain.staff.StaffUser;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@AllArgsConstructor
@Service
public class AuthService {
    private final CustomerAuthRepository customerRepo;
    private final StaffAuthRepository staffRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProps;
    private final CustomerIdentityRepository customerIdentityRepository;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;

    public TokenResponse customerGoogleLogin(GoogleLoginRequest req){
        var oidc = googleIdTokenVerifierService.verify(req.idToken());

        var existing = customerIdentityRepository.findByProviderAndProviderSubject(
                OauthProvider.GOOGLE,
                oidc.subject()
        );
        Customer customer;
        if (existing.isPresent()) {
            customer = existing.get().getCustomer();
        } else {
            // 2) "Signup" on first login
            customer = new Customer();
            customer.setStatus(CustomerStatus.ACTIVE);

            // Optional fields (depends on your Customer entity)
            if (oidc.email() != null && !oidc.email().isBlank()) {
                customer.setEmail(oidc.email());
            }

            if (oidc.fullName() != null && !oidc.fullName().isBlank()) {
                customer.setDisplayName(oidc.fullName());
            }

            customer = customerRepo.save(customer);

            var ident = new CustomerIdentity();
            ident.setCustomer(customer);
            ident.setProvider(OauthProvider.GOOGLE);
            ident.setProviderSubject(oidc.subject());
            ident.setEmail(oidc.email());
            customerIdentityRepository.save(ident);
        }

        if (customer.getStatus() == CustomerStatus.DISABLED) {
            throw new UnauthorizedException("Customer is disabled");
        }

        var principal = new AuthPrincipal(customer.getId(), UserType.CUSTOMER, null);
        var roles = List.of("ROLE_CUSTOMER");
        String token = jwtService.mintAccessToken(principal, roles);

        return new TokenResponse(
                token,
                "Bearer",
                jwtProps.accessTokenSeconds(),
                customer.getId(),
                com.beta.loyalty.auth.UserType.CUSTOMER.name(),
                null,
                roles
        );

    }

    public TokenResponse customerLogin(LoginRequest req) {
        Customer customer = findCustomerByIdentifier(req.identifier());
        if (customer.getStatus() == CustomerStatus.DISABLED) {
            throw new UnauthorizedException("Customer is disabled");
        }
        if (!passwordEncoder.matches(req.password(), customer.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        var principal = new AuthPrincipal(customer.getId(), UserType.CUSTOMER, null);
        var roles = List.of("ROLE_CUSTOMER");
        String token = jwtService.mintAccessToken(principal, roles);

        return new TokenResponse(
                token,
                "Bearer",
                jwtProps.accessTokenSeconds(),
                customer.getId(),
                UserType.CUSTOMER.name(),
                null,
                roles
        );
    }

    public TokenResponse staffLogin(LoginRequest req) {
        StaffUser staff = findStaffByIdentifier(req.identifier());
        if (staff.getStatus() == StaffStatus.DISABLED) {
            throw new UnauthorizedException("Staff user is disabled");
        }
        if (!passwordEncoder.matches(req.password(), staff.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Map your Role entities to ROLE_* strings.
        // If staff.roles contains "OWNER", "MANAGER", etc, we still also include ROLE_STAFF.
        List<String> roles = staff.getRoles().stream()
                .map(r -> "ROLE_" + r.getName().toUpperCase())
                .distinct()
                .toList();

        // Ensure base role
        if (roles.stream().noneMatch(r -> r.equals("ROLE_STAFF"))) {
            roles = concat(roles, "ROLE_STAFF");
        }

        var principal = new AuthPrincipal(staff.getId(), UserType.STAFF, staff.getTenant().getId());
        String token = jwtService.mintAccessToken(principal, roles);

        return new TokenResponse(
                token,
                "Bearer",
                jwtProps.accessTokenSeconds(),
                staff.getId(),
                UserType.STAFF.name(),
                staff.getTenant().getId(),
                roles
        );
    }
    private Customer findCustomerByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) throw new UnauthorizedException("Invalid credentials");

        return (identifier.contains("@")
                ? customerRepo.findByEmailIgnoreCase(identifier)
                : customerRepo.findByPhone(identifier)
        ).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    }

    private StaffUser findStaffByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) throw new UnauthorizedException("Invalid credentials");

        return (identifier.contains("@")
                ? staffRepo.findByEmailIgnoreCase(identifier)
                : staffRepo.findByPhone(identifier)
        ).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    }

    private static List<String> concat(List<String> roles, String extra) {
        var out = new java.util.ArrayList<>(roles);
        out.add(extra);
        return out;
    }

}
