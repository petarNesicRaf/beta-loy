package com.beta.loyalty.friends.service;

import com.beta.loyalty.customer.repository.CustomerRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.customer.CustomerFriendship;
import com.beta.loyalty.domain.enums.FriendshipStatus;
import com.beta.loyalty.friends.dto.CustomerSearchItemDto;
import com.beta.loyalty.friends.repository.CustomerFriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerSearchService {

    private final CustomerRepository customerRepository;
    private final CustomerFriendshipRepository friendshipRepository;

    @Transactional(readOnly = true)
    public Page<CustomerSearchItemDto> searchByUsername(UUID me, String q, Pageable pageable) {
        String query = (q == null) ? "" : q.trim().toLowerCase();
        if (query.length() < 2) { // anti-enumeration guard (tune as you like)
            return Page.empty(pageable);
        }

        Page<Customer> page = customerRepository.searchByUsernamePrefix(query, pageable);

        // exclude me
        List<Customer> customers = page.getContent().stream()
                .filter(c -> !c.getId().equals(me))
                .toList();

        if (customers.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }

        List<UUID> otherIds = customers.stream().map(Customer::getId).toList();
        List<CustomerFriendship> rels = friendshipRepository.findBetweenMeAndOthers(me, otherIds);

        Map<UUID, CustomerFriendship> byOtherId = rels.stream()
                .collect(Collectors.toMap(
                        f -> otherId(me, f),
                        Function.identity(),
                        (a, b) -> a
                ));

        List<CustomerSearchItemDto> dtoList = customers.stream()
                .map(c -> {
                    CustomerFriendship f = byOtherId.get(c.getId());
                    if (f == null) {
                        return new CustomerSearchItemDto(c.getId(), c.getUsername(), c.getDisplayName(),
                                null, false, false);
                    }
                    boolean requestedByMe = f.getRequestedBy().getId().equals(me);
                    boolean outgoing = f.getStatus() == FriendshipStatus.PENDING && requestedByMe;
                    boolean incoming = f.getStatus() == FriendshipStatus.PENDING && !requestedByMe;

                    return new CustomerSearchItemDto(
                            c.getId(),
                            c.getUsername(),
                            c.getDisplayName(),
                            f.getStatus(),
                            outgoing,
                            incoming
                    );
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    private UUID otherId(UUID me, CustomerFriendship f) {
        UUID a = f.getCustomerA().getId();
        UUID b = f.getCustomerB().getId();
        return me.equals(a) ? b : a;
    }
}
