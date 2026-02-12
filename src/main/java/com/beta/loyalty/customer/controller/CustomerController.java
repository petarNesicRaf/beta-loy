package com.beta.loyalty.customer.controller;

import com.beta.loyalty.customer.dto.CustomerMeResponse;
import com.beta.loyalty.customer.dto.CustomerVenuePointsAccount;
import com.beta.loyalty.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/details")
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
