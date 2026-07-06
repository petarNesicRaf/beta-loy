package com.beta.loyalty.controller;

import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.dto.CustomerMeResponse;
import com.beta.loyalty.dto.CustomerVenuePointsAccount;
import com.beta.loyalty.service.CustomerService;
import com.beta.loyalty.dto.CreateRedemptionRequestDto;
import com.beta.loyalty.dto.RedemptionRequestDto;
import com.beta.loyalty.service.RedemptionRequestService;
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
