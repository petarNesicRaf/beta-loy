package com.beta.loyalty.redemptions.controller;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.redemptions.dto.CreateRedemptionRequestDto;
import com.beta.loyalty.redemptions.dto.RedemptionRequestDto;
import com.beta.loyalty.redemptions.service.RedemptionRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/redemptions")
@Tag(
        name = "Customer Redemption",
        description = "Ovo je controller za slanje request-a za nagradu flow za to je:\n" +
                " POST /api/v1/customer/redemptions/venues/{venueId}/create -> kreira PENDING request za nagradu\n"+
                " Staff-u se ubacuje u listu koju polluje svakih 3-5 sek- za pocetak \n"+
                " GET /api/v1/staff/venues/{venueId}/redemptions/pending \n" +
                " Staff odlucuje sta ce da radi sa request-om \n" +
                " POST /api/v1/staff/redemptions/{id}/decide \n" +
                " Staff donosi odluku i salje je na fullfill (ako je approve, dekrementira poene useru, stavi status APPROVED, opciono dekrementira lager za tu nagradu\n"
                + " POST /api/v1/staff/redemptions/{id}/fulfill - zavrsava transakciju\n "
)

public class CustomerRedemptionController {

    private final RedemptionRequestService redemptionRequestService;

    @PostMapping("/venues/{venueId}/create")
    public RedemptionRequestDto create(
            @PathVariable UUID venueId,
            @RequestBody CreateRedemptionRequestDto req
    ) {
        UUID currUuid = CurrentUser.principal().get().userId();
        return redemptionRequestService.create(currUuid, venueId, req);
    }
}
