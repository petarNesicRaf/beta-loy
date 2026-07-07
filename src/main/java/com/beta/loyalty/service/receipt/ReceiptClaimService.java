package com.beta.loyalty.service.receipt;

import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.repository.auth.CustomerAuthRepository;
import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.enums.ReceiptClaimStatus;
import com.beta.loyalty.domain.enums.ReceiptStatus;
import com.beta.loyalty.domain.Receipt;
import com.beta.loyalty.domain.ReceiptClaim;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.service.points.PointsService;
import com.beta.loyalty.dto.receipt.ReceiptClaimRequest;
import com.beta.loyalty.dto.receipt.ReceiptClaimResponse;
import com.beta.loyalty.repository.receipt.ReceiptClaimRepository;
import com.beta.loyalty.repository.receipt.ReceiptRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
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
    private final CustomerAuthRepository customerAuthRepository;
    private final VenueRepository venueRepository;
    private final PointsService pointsService;

    //scan tj ulazna tacka za racune
    //pravi racun, pravi claim,
    // prosledjuje point servisu, servis vraca zaradjene poene u ReceiptClaimResponse
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
        Customer customerRef = customerAuthRepository.getReferenceById(customerId);
        Venue venueRef = venueRepository.getReferenceById(req.venueId());
        String venuePib = normalizePib(venueRef.getPib());
        String reqPib   = normalizePib(req.pib());

        if (venuePib == null || venuePib.isBlank()) {
            throw new com.beta.loyalty.exception.ConflictException("Venue PIB is not configured");
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

        // ako racun ne postoji u bazi insertuje ga, u suprotnom je zakljucan
        Receipt receipt = lockOrCreateReceipt(venueRef, req, receiptHash);

        ReceiptClaim claim = lockOrCreateClaim(customerRef, venueRef, receipt);

        // If claim already finalized/approved earlier, return without re-awarding.
        // (Ledger idempotency also protects, but returning early is cleaner.)
        // todo bolje da vrati neuspesna transakcija nego 0 poena,uspesna
        if (claim.getFinalizedAt() != null) {
            return new ReceiptClaimResponse(receipt.getId(), claim.getId(), claim.getStatus().name(), 0L);
        }

        // kalkulisanje poena 100din -> 1 poen
        long points = calculatePointsMvp(req.amount());

        claim.setCalculatedPointsTotal(points);

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




    private long calculatePointsMvp(BigDecimal amount) {
        // Example: 1 point per 100
        return amount.divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).longValue();
    }
    private String normalizePib(String pib) {
        if (pib == null) return null;
        // keep only digits (PIB is numeric)
        return pib.replaceAll("\\D", "");
    }

}
