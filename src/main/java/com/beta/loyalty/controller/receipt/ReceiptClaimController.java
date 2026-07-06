package com.beta.loyalty.controller.receipt;

import com.beta.loyalty.dto.receipt.ReceiptClaimRequest;
import com.beta.loyalty.dto.receipt.ReceiptClaimResponse;
import com.beta.loyalty.service.receipt.ReceiptClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/receipts")
@Tag(
        name = "Receipt Claim",
        description = "Sluzi za sad za fejk skeniranje (claim) fiskalnih racuna"
)
public class ReceiptClaimController {
    private final ReceiptClaimService receiptClaimService;

    @PostMapping("/claim")
    public ReceiptClaimResponse claim(@Valid @RequestBody ReceiptClaimRequest req) {
        // For now: mimic auth by header
        // Later: replace with @AuthenticationPrincipal and pull customerId from JWT
        return receiptClaimService.claimIndividual(req);
    }
}
