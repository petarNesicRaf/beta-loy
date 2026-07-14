package com.beta.loyalty.service.customer;

import com.beta.loyalty.dto.customer.CustomerBalanceResponse;
import com.beta.loyalty.dto.customer.StaffCustomerResponse;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.domain.RedemptionRequest;
import com.beta.loyalty.exception.ForbiddenException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.customer.CustomerRepository;
import com.beta.loyalty.repository.points.PointsAccountRepository;
import com.beta.loyalty.repository.redemption.RedemptionRequestRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffCustomerService {

    private final CustomerRepository customerRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final RedemptionRequestRepository redemptionRequestRepository;
    private final VenueStaffAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public Page<StaffCustomerResponse> search(String q, Pageable pageable) {
        return customerRepository.searchByEmailOrUsername(q, pageable)
                .map(StaffCustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public CustomerBalanceResponse getBalance(UUID staffUserId, UUID customerId, UUID venueId) {
        requireAssignment(staffUserId, venueId);
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found");
        }
        long balance = pointsAccountRepository.findBalance(customerId, venueId).orElse(0L);
        return new CustomerBalanceResponse(customerId, venueId, balance);
    }

    @Transactional(readOnly = true)
    public Page<RedemptionRequestDto> getCustomerRedemptions(
            UUID staffUserId, UUID customerId, UUID venueId, Pageable pageable) {
        requireAssignment(staffUserId, venueId);
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found");
        }
        return redemptionRequestRepository
                .findByCustomerIdAndVenueId(customerId, venueId, pageable)
                .map(this::toDto);
    }

    private RedemptionRequestDto toDto(RedemptionRequest rr) {
        return new RedemptionRequestDto(
                rr.getId(), rr.getVenue().getId(), rr.getReward().getId(),
                rr.getCustomer().getId(), rr.getStatus(), rr.getPointsCostSnapshot(),
                rr.getRequestedAt(), rr.getExpiresAt(), rr.getApprovedAt(),
                rr.getRejectedAt(), rr.getFulfilledAt()
        );
    }

    private void requireAssignment(UUID staffUserId, UUID venueId) {
        if (!assignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to this venue");
        }
    }
}
