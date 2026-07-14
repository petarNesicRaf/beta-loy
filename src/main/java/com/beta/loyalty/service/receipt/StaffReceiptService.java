package com.beta.loyalty.service.receipt;

import com.beta.loyalty.domain.Receipt;
import com.beta.loyalty.domain.enums.ReceiptStatus;
import com.beta.loyalty.dto.receipt.ReceiptResponse;
import com.beta.loyalty.exception.ConflictException;
import com.beta.loyalty.exception.ForbiddenException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.receipt.ReceiptRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffReceiptService {

    private final ReceiptRepository receiptRepository;
    private final VenueStaffAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public Page<ReceiptResponse> listReceipts(UUID staffUserId, UUID tenantId, UUID venueId, Pageable pageable) {
        requireAssignment(staffUserId, venueId);
        return receiptRepository.findByVenueIdAndTenantId(venueId, tenantId, pageable)
                .map(ReceiptResponse::from);
    }

    @Transactional
    public ReceiptResponse voidReceipt(UUID staffUserId, UUID tenantId, UUID receiptId) {
        Receipt receipt = receiptRepository.findByIdAndTenantId(receiptId, tenantId)
                .orElseThrow(() -> new NotFoundException("Receipt not found"));
        requireAssignment(staffUserId, receipt.getVenue().getId());
        if (receipt.getStatus() == ReceiptStatus.VOID) {
            throw new ConflictException("Receipt is already void");
        }
        receipt.setStatus(ReceiptStatus.VOID);
        return ReceiptResponse.from(receiptRepository.save(receipt));
    }

    private void requireAssignment(UUID staffUserId, UUID venueId) {
        if (!assignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to this venue");
        }
    }
}
