package com.beta.loyalty.customer.controller;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.customer.dto.CustomerMeResponse;
import com.beta.loyalty.customer.dto.CustomerVenuePointsAccount;
import com.beta.loyalty.customer.service.CustomerService;
import com.beta.loyalty.redemptions.dto.CreateRedemptionRequestDto;
import com.beta.loyalty.redemptions.dto.RedemptionRequestDto;
import com.beta.loyalty.redemptions.service.RedemptionRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer")
@Tag(
        name = "Customer",
        description = "Operacije vezane za musteriju, izvlacenje poena po lokalu"
)
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/me")
    public CustomerMeResponse me(){
        return customerService.me();
    }

    @GetMapping("/points-per-venue")
    public List<CustomerVenuePointsAccount> pointsPerVenue(){
        return customerService.pointsAccPerVenue();
    }


}
