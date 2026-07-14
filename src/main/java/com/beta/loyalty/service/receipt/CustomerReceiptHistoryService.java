package com.beta.loyalty.service.receipt;

import com.beta.loyalty.dto.receipt.CustomerReceiptClaimResponse;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.receipt.ReceiptClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerReceiptHistoryService {

    private final ReceiptClaimRepository receiptClaimRepository;

    @Transactional(readOnly = true)
    public Page<CustomerReceiptClaimResponse> getHistory(UUID customerId, Pageable pageable) {
        return receiptClaimRepository
                .findByInitiatorCustomer_IdOrderByCreatedAtDesc(customerId, pageable)
                .map(CustomerReceiptClaimResponse::from);
    }

    @Transactional(readOnly = true)
    public CustomerReceiptClaimResponse getOne(UUID customerId, UUID claimId) {
        return receiptClaimRepository
                .findByIdAndInitiatorCustomer_Id(claimId, customerId)
                .map(CustomerReceiptClaimResponse::from)
                .orElseThrow(() -> new NotFoundException("Receipt claim not found"));
    }
}
