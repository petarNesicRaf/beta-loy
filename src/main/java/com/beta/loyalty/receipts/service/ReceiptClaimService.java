package com.beta.loyalty.receipts.service;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.auth.jwt.repo.CustomerRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.enums.ReceiptClaimStatus;
import com.beta.loyalty.domain.enums.ReceiptStatus;
import com.beta.loyalty.domain.receipt.Receipt;
import com.beta.loyalty.domain.receipt.ReceiptClaim;
import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.points.service.PointsService;
import com.beta.loyalty.receipts.dto.ReceiptClaimRequest;
import com.beta.loyalty.receipts.dto.ReceiptClaimResponse;
import com.beta.loyalty.receipts.repository.ReceiptClaimRepository;
import com.beta.loyalty.receipts.repository.ReceiptRepository;
import com.beta.loyalty.venues.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptClaimService {
    private final ReceiptRepository receiptRepository;
    private final ReceiptClaimRepository claimRepository;
    private final CustomerRepository customerRepository;
    private final VenueRepository venueRepository;
    private final PointsService pointsService;


    @Transactional
    public ReceiptClaimResponse claimIndividual(ReceiptClaimRequest req) {
        UUID customerId = CurrentUser.requirePrincipal().userId();

        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (req.currency() == null || req.currency().isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        if (req.issuedAt() == null) {
            throw new IllegalArgumentException("issuedAt is required");
        }
        if (req.venueId() == null) {
            throw new IllegalArgumentException("venueId is required");
        }
        if (req.pib() == null || req.pib().isBlank()) {
            throw new IllegalArgumentException("pib is required");
        }

        // References (fast)
        Customer customerRef = customerRepository.getReferenceById(customerId);
        Venue venueRef = venueRepository.getReferenceById(req.venueId());
        String venuePib = normalizePib(venueRef.getPib());
        String reqPib   = normalizePib(req.pib());

        if (venuePib == null || venuePib.isBlank()) {
            throw new IllegalStateException("Venue PIB is not configured");
        }

        if (!venuePib.equals(reqPib)) {
            throw new IllegalArgumentException("Receipt PIB does not match venue");
        }

        // 1) compute receipt hash
        String receiptHash = ReceiptHashing.sha256ReceiptHash(
                venueRef.getId(),
                req.externalReceiptId(),
                req.qrRaw(),
                req.issuedAt().toInstant().toString(),
                req.amount().stripTrailingZeros().toPlainString(),
                req.currency()
        );

        // 2) lock or create receipt (idempotent)
        Receipt receipt = lockOrCreateReceipt(venueRef, req, receiptHash);

        // 3) lock or create claim (idempotent)
        ReceiptClaim claim = lockOrCreateClaim(customerRef, venueRef, receipt);

        // If claim already finalized/approved earlier, return without re-awarding.
        // (Ledger idempotency also protects, but returning early is cleaner.)
        if (claim.getFinalizedAt() != null) {
            return new ReceiptClaimResponse(receipt.getId(), claim.getId(), claim.getStatus().name(), 0L);
        }

        // 4) calculate points (MVP rule: 1 point per 100 currency units)
        long points = calculatePointsMvp(req.amount());

        claim.setCalculatedPointsTotal(points);

        // For MVP, auto-finalize immediately:
        claim.setStatus(ReceiptClaimStatus.FINALIZED);
        claim.setFinalizedAt(java.time.OffsetDateTime.now());
        claimRepository.save(claim);

        // 5) award points (ledger idempotent by (sourceType, sourceId, customerId))
        pointsService.earn(
                customerId,
                venueRef.getId(),
                points,
                LedgerSourceType.RECEIPT_CLAIM,
                claim.getId()
        );

        return new ReceiptClaimResponse(receipt.getId(), claim.getId(), claim.getStatus().name(), points);
    }

    private long calculatePointsMvp(BigDecimal amount) {
        // Example: 1 point per 100
        return amount.divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).longValue();
    }
    private String normalizePib(String pib) {
        if (pib == null) return null;
        // keep only digits (PIB is numeric)
        return pib.replaceAll("\\D", "");
    }

    private Receipt lockOrCreateReceipt(Venue venueRef, ReceiptClaimRequest req, String receiptHash) {
        var locked = receiptRepository.findForUpdate(venueRef.getId(), receiptHash);
        if (locked.isPresent()) return locked.get();

        try {
            Receipt created = new Receipt();
            created.setVenue(venueRef);
            created.setPib(venueRef.getPib());
            created.setTenantId(venueRef.getTenant().getId()); // adapt if your TenantOwnedEntity uses tenantId setter
            created.setReceiptHash(receiptHash);
            created.setExternalReceiptId(req.externalReceiptId());
            created.setIssuedAt(req.issuedAt());
            created.setAmount(req.amount());
            created.setCurrency(req.currency());
            created.setStatus(ReceiptStatus.VALID);

            receiptRepository.saveAndFlush(created);

            return receiptRepository.findForUpdate(venueRef.getId(), receiptHash)
                    .orElseThrow(() -> new IllegalStateException("Receipt created but not found"));
        } catch (DataIntegrityViolationException e) {
            // Someone else inserted it concurrently
            return receiptRepository.findForUpdate(venueRef.getId(), receiptHash)
                    .orElseThrow(() -> new IllegalStateException("Receipt exists but not found"));
        }
    }

    private ReceiptClaim lockOrCreateClaim(Customer customerRef, Venue venueRef, Receipt receipt) {
        // We don't have a "FOR UPDATE" query for claim by receipt; unique(receipt_id) handles concurrency.
        var existing = claimRepository.findByReceipt_Id(receipt.getId());
        if (existing.isPresent()) return existing.get();

        try {
            ReceiptClaim created = new ReceiptClaim();
            created.setTenantId(venueRef.getTenant().getId()); // adapt to your TenantOwnedEntity
            created.setVenue(venueRef);
            created.setReceipt(receipt);
            created.setInitiatorCustomer(customerRef);
            created.setStatus(ReceiptClaimStatus.FINALIZED); // will be overwritten anyway; safe default

            claimRepository.saveAndFlush(created);

            return claimRepository.findByReceipt_Id(receipt.getId())
                    .orElseThrow(() -> new IllegalStateException("Claim created but not found"));
        } catch (DataIntegrityViolationException e) {
            return claimRepository.findByReceipt_Id(receipt.getId())
                    .orElseThrow(() -> new IllegalStateException("Claim exists but not found"));
        }
    }
}
