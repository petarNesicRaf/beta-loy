package com.beta.loyalty.customer.controller;

import com.beta.loyalty.customer.dto.CustomerMeResponse;
import com.beta.loyalty.customer.dto.CustomerVenuePointsAccount;
import com.beta.loyalty.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/details")
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
