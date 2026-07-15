package com.beta.loyalty.service.customer;

import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.PointsAccount;
import com.beta.loyalty.domain.enums.CustomerStatus;
import com.beta.loyalty.dto.customer.ChangeCustomerPasswordRequest;
import com.beta.loyalty.dto.customer.CustomerMeResponse;
import com.beta.loyalty.dto.customer.CustomerStatsResponse;
import com.beta.loyalty.dto.customer.CustomerVenuePointsAccount;
import com.beta.loyalty.dto.customer.UpdateCustomerProfileRequest;
import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.exception.ConflictException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.exception.UnauthorizedException;
import com.beta.loyalty.repository.customer.CustomerRepository;
import com.beta.loyalty.repository.points.PointsAccountRepository;
import com.beta.loyalty.repository.redemption.RedemptionRequestRepository;
import com.beta.loyalty.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final RedemptionRequestRepository redemptionRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public CustomerMeResponse me() {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        return CustomerMeResponse.from(c);
    }

    @Transactional
    public CustomerMeResponse updateProfile(UUID customerId, UpdateCustomerProfileRequest req) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        if (req.username() != null && !req.username().equals(c.getUsername())) {
            customerRepository.findByUsernameIgnoreCase(req.username())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(customerId)) {
                            throw new ConflictException("Username already taken");
                        }
                    });
            c.setUsername(req.username());
        }
        if (req.displayName() != null) c.setDisplayName(req.displayName());
        if (req.phone() != null) c.setPhone(req.phone());

        return CustomerMeResponse.from(customerRepository.save(c));
    }

    @Transactional
    public void changePassword(UUID customerId, ChangeCustomerPasswordRequest req) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        if (c.getPasswordHash() == null) {
            throw new ConflictException("Account uses social login — password change not supported");
        }
        if (!passwordEncoder.matches(req.currentPassword(), c.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        c.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        customerRepository.save(c);
    }

    @Transactional
    public void deleteAccount(UUID customerId) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        c.setStatus(CustomerStatus.DISABLED);
        customerRepository.save(c);
    }

    @Transactional(readOnly = true)
    public CustomerStatsResponse getStats() {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        long totalPoints = pointsAccountRepository.sumBalanceByCustomerId(customerId);
        long venuesCount = pointsAccountRepository.countByCustomerId(customerId);
        long rewardsRedeemed = redemptionRequestRepository
                .countByCustomerIdAndStatus(customerId, RedemptionStatus.FULFILLED);
        return new CustomerStatsResponse(totalPoints, venuesCount, rewardsRedeemed);
    }

    @Transactional(readOnly = true)
    public List<CustomerVenuePointsAccount> pointsAccPerVenue() {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        List<PointsAccount> accounts = pointsAccountRepository
                .findByCustomer_IdAndCurrentBalanceGreaterThan(customerId, 0L);
        return accounts.stream()
                .map(pa -> new CustomerVenuePointsAccount(
                        pa.getVenue().getId(),
                        pa.getVenue().getName(),
                        pa.getCurrentBalance()
                )).toList();
    }
}
