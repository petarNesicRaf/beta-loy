package com.beta.loyalty.customer.service;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.customer.dto.CustomerMeResponse;
import com.beta.loyalty.customer.dto.CustomerVenuePointsAccount;
import com.beta.loyalty.customer.repository.CustomerRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.points.PointsAccount;
import com.beta.loyalty.points.repository.PointsAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PointsAccountRepository pointsAccountRepository;
    @Transactional(readOnly = true)
    public CustomerMeResponse me() {
        UUID customerId = CurrentUser.requirePrincipal().userId();

        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found"));

        return new CustomerMeResponse(
                c.getId(),
                c.getEmail(),
                c.getDisplayName()
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerVenuePointsAccount> pointsAccPerVenue(){
        UUID customerId = CurrentUser.requirePrincipal().userId();
        List<PointsAccount> accounts  = pointsAccountRepository
                .findByCustomer_IdAndCurrentBalanceGreaterThan(customerId, 0L);

        return accounts.stream()
                .map(pa->new CustomerVenuePointsAccount(
                        pa.getVenue().getId(),
                        pa.getVenue().getName(),
                        pa.getCurrentBalance()
                )).toList();
    }
}
