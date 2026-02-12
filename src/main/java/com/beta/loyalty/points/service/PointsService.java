package com.beta.loyalty.points.service;

import com.beta.loyalty.auth.jwt.repo.CustomerAuthRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.enums.LedgerType;
import com.beta.loyalty.domain.points.PointsAccount;
import com.beta.loyalty.domain.points.PointsLedgerEntry;
import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.points.repository.PointsAccountRepository;
import com.beta.loyalty.points.repository.PointsLedgerEntryRepository;
import com.beta.loyalty.venues.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointsService {
    private final PointsAccountRepository pointsAccountRepository;
    private final PointsLedgerEntryRepository ledgerRepository;
    private final CustomerAuthRepository customerAuthRepository;
    private final VenueRepository venueRepository;


    @Transactional
    public void earn(UUID customerId, UUID venueId, long points,
                     LedgerSourceType sourceType, UUID sourceId) {

        if (points <= 0) throw new IllegalArgumentException("points must be > 0");

        applyDelta(customerId, venueId, points, LedgerType.EARN, sourceType, sourceId);
    }
    /**
     * Core pipeline:
     * 1) idempotency check
     * 2) lock or create account
     * 3) validate sufficient funds (for spend)
     * 4) create ledger entry
     * 5) update balance
     */
    private void applyDelta(UUID customerId, UUID venueId, long delta,
                            LedgerType ledgerType,
                            LedgerSourceType sourceType, UUID sourceId) {

        // 1) Idempotency (fast path)
        //tip transakcije, id-racuna, customer-id
        if (ledgerRepository.existsBySourceTypeAndSourceIdAndCustomerId(sourceType, sourceId, customerId)) {
            return;
        }

        // Use references for performance; will throw if missing when flushed
        Customer customerRef = customerAuthRepository.getReferenceById(customerId);
        Venue venueRef = venueRepository.getReferenceById(venueId);

        // (optional but recommended) ensure venue is ACTIVE for customer flows:
        // if (!venueRepository.existsByIdAndStatus(venueId, VenueStatus.ACTIVE)) throw ...

        // 2) Lock or create account (concurrency-safe)
        //kreiraj ili nadji account za poseban venue
        PointsAccount account = lockOrCreateAccount(customerRef, venueRef);

        // 3) For spend, prevent negative
        long newBalance = account.getCurrentBalance() + delta;
        if (newBalance < 0) {
            throw new IllegalStateException("Insufficient points");
        }

        // 4) Create ledger entry
        //nakon svake transakcije cuvaj to u ledger
        PointsLedgerEntry entry = new PointsLedgerEntry();
        entry.setPointsAccount(account);
        entry.setCustomer(customerRef);
        entry.setVenue(venueRef);
        entry.setType(ledgerType);
        entry.setPointsDelta(delta);
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setTenantId(venueRef.getTenant().getId());

        // 4b) TenantOwnedEntity: derive tenant from venue (if your TenantOwnedEntity requires it)
        // If your TenantOwnedEntity has setTenantId(UUID) or setTenant(Tenant), set it here:
//        Tenant tenant = venueRef.getTenant(); // derived, hidden from customer
        // entry.setTenant(tenant); or entry.setTenantId(tenant.getId());  <-- adapt to your base class

        ledgerRepository.save(entry);

        // 5) Update balance
        account.setCurrentBalance(newBalance);
        pointsAccountRepository.save(account);
    }
    /**
     * Pessimistic lock when existing, create if missing.
     * Uses UNIQUE (customer_id, venue_id) to handle concurrent create safely.
     */
    private PointsAccount lockOrCreateAccount(Customer customer, Venue venue) {
        // Try lock existing first
        var locked = pointsAccountRepository.findForUpdate(customer.getId(), venue.getId());
        if (locked.isPresent()) return locked.get();

        // Not found: create
        try {
            PointsAccount created = new PointsAccount();
            created.setCustomer(customer);
            created.setVenue(venue);
            created.setCurrentBalance(0L);

            // save+flush to materialize row and enforce unique constraint now
            pointsAccountRepository.saveAndFlush(created);

            // Re-lock it (consistent path)
            return pointsAccountRepository.findForUpdate(customer.getId(), venue.getId())
                    .orElseThrow(() -> new IllegalStateException("PointsAccount created but not found"));
        } catch (DataIntegrityViolationException e) {
            // Another request created it between our read and insert
            return pointsAccountRepository.findForUpdate(customer.getId(), venue.getId())
                    .orElseThrow(() -> new IllegalStateException("PointsAccount exists but not found"));
        }
    }
}
