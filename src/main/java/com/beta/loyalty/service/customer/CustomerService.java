package com.beta.loyalty.service.customer;

import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.dto.customer.CustomerMeResponse;
import com.beta.loyalty.dto.customer.CustomerVenuePointsAccount;
import com.beta.loyalty.repository.customer.CustomerRepository;
import com.beta.loyalty.domain.Customer;
import com.beta.loyalty.domain.PointsAccount;
import com.beta.loyalty.repository.points.PointsAccountRepository;
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


    //vraca sve zaradjene poene za sve lokale gde je korisnik zaradjivao poene
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
