package com.beta.loyalty.controller.receipt;

import com.beta.loyalty.dto.receipt.ReceiptResponse;
import com.beta.loyalty.security.AuthPrincipal;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.receipt.StaffReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff/receipts")
@Tag(name = "Staff - Receipts", description = "Void fraudulent receipts")
public class StaffReceiptController {

    private final StaffReceiptService staffReceiptService;

    @PatchMapping("/{receiptId}/void")
    public ReceiptResponse voidReceipt(@PathVariable UUID receiptId) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffReceiptService.voidReceipt(p.userId(), p.tenantId(), receiptId);
    }
}
