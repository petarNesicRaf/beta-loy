package com.beta.loyalty.controller.customer;

import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.dto.customer.CustomerMeResponse;
import com.beta.loyalty.dto.customer.CustomerVenuePointsAccount;
import com.beta.loyalty.service.customer.CustomerService;
import com.beta.loyalty.dto.redemption.CreateRedemptionRequestDto;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.service.redemption.RedemptionRequestService;
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

    //vraca profil
    @GetMapping("/me")
    public CustomerMeResponse me(){
        return customerService.me();
    }


    @GetMapping("/points-per-venue")
    public List<CustomerVenuePointsAccount> pointsPerVenue(){
        return customerService.pointsAccPerVenue();
    }


}
