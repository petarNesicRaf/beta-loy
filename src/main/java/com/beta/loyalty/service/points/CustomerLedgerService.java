package com.beta.loyalty.service.points;

import com.beta.loyalty.dto.points.LedgerEntryResponse;
import com.beta.loyalty.repository.points.PointsLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerLedgerService {

    private final PointsLedgerEntryRepository ledgerRepository;

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getHistory(UUID customerId, UUID venueId, Pageable pageable) {
        return ledgerRepository
                .findByCustomerIdAndOptionalVenue(customerId, venueId, pageable)
                .map(LedgerEntryResponse::from);
    }
}
