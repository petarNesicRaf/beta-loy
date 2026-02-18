package com.beta.loyalty.friends.service;

import com.beta.loyalty.customer.repository.CustomerRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.customer.CustomerFriendship;
import com.beta.loyalty.domain.enums.FriendshipStatus;
import com.beta.loyalty.friends.dto.FriendshipDto;
import com.beta.loyalty.friends.dto.FriendshipPair;
import com.beta.loyalty.friends.repository.CustomerFriendshipRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final CustomerRepository customerRepository;
    private final CustomerFriendshipRepository friendshipRepository;


    @Transactional
    public FriendshipDto sendRequest(UUID me, UUID targetCustomerId) {
        if (targetCustomerId == null) throw new IllegalArgumentException("targetCustomerId is required");
        if (me.equals(targetCustomerId)) throw new IllegalArgumentException("Cannot friend yourself");

        Customer meEntity = customerRepository.findById(me)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: me"));
        Customer target = customerRepository.findById(targetCustomerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: target"));

        FriendshipPair.Pair pair = FriendshipPair.canonical(me, targetCustomerId);

        CustomerFriendship existing = friendshipRepository
                .findByCustomerAIdAndCustomerBId(pair.a(), pair.b())
                .orElse(null);

        if (existing != null) {
            if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                return toDto(me, existing);
            }
            if (existing.getStatus() == FriendshipStatus.PENDING) {
                return toDto(me, existing);
            }
            if (existing.getStatus() == FriendshipStatus.BLOCKED) {
                throw new IllegalStateException("Friendship is blocked");
            }

            // For DECLINED you can either re-open or block re-request.
            // Here: re-open as PENDING.
            existing.setStatus(FriendshipStatus.PENDING);
            existing.setRequestedBy(meEntity);
            existing.setRespondedAt(null);
            return toDto(me, friendshipRepository.save(existing));
        }

        CustomerFriendship f = new CustomerFriendship();
        f.setCustomerA(customerRepository.getReferenceById(pair.a()));
        f.setCustomerB(customerRepository.getReferenceById(pair.b()));
        f.setStatus(FriendshipStatus.PENDING);
        f.setRequestedBy(meEntity);
        f.setRespondedAt(null);

        return toDto(me, friendshipRepository.save(f));
    }

    @Transactional
    public FriendshipDto accept(UUID me, UUID friendshipId) {
        CustomerFriendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        ensureParticipant(me, f);

        if (f.getStatus() != FriendshipStatus.PENDING) {
            return toDto(me, f);
        }
        if (f.getRequestedBy().getId().equals(me)) {
            throw new IllegalArgumentException("You cannot accept your own request");
        }

        f.setStatus(FriendshipStatus.ACCEPTED);
        f.setRespondedAt(OffsetDateTime.now());
        return toDto(me, friendshipRepository.save(f));
    }

    @Transactional
    public FriendshipDto decline(UUID me, UUID friendshipId) {
        CustomerFriendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        ensureParticipant(me, f);

        if (f.getStatus() != FriendshipStatus.PENDING) {
            return toDto(me, f);
        }
        if (f.getRequestedBy().getId().equals(me)) {
            throw new IllegalArgumentException("You cannot decline your own request (cancel instead)");
        }

        f.setStatus(FriendshipStatus.DECLINED);
        f.setRespondedAt(OffsetDateTime.now());
        return toDto(me, friendshipRepository.save(f));
    }

    @Transactional
    public FriendshipDto cancel(UUID me, UUID friendshipId) {
        CustomerFriendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        ensureParticipant(me, f);

        if (f.getStatus() != FriendshipStatus.PENDING) {
            return toDto(me, f);
        }
        if (!f.getRequestedBy().getId().equals(me)) {
            throw new IllegalArgumentException("Only requester can cancel");
        }

        f.setStatus(FriendshipStatus.DECLINED); // or delete row if you prefer
        f.setRespondedAt(OffsetDateTime.now());
        return toDto(me, friendshipRepository.save(f));
    }

    @Transactional(readOnly = true)
    public Page<FriendshipDto> listAccepted(UUID me, Pageable pageable) {
        return friendshipRepository.findMineByStatus(me, FriendshipStatus.ACCEPTED, pageable)
                .map(f -> toDto(me, f));
    }

    @Transactional(readOnly = true)
    public Page<FriendshipDto> listIncomingPending(UUID me, Pageable pageable) {
        return friendshipRepository.findIncomingPending(me, pageable)
                .map(f -> toDto(me, f));
    }

    @Transactional(readOnly = true)
    public Page<FriendshipDto> listOutgoingPending(UUID me, Pageable pageable) {
        return friendshipRepository.findOutgoingPending(me, pageable)
                .map(f -> toDto(me, f));
    }

    private void ensureParticipant(UUID me, CustomerFriendship f) {
        UUID a = f.getCustomerA().getId();
        UUID b = f.getCustomerB().getId();
        if (!me.equals(a) && !me.equals(b)) {
            throw new IllegalArgumentException("Not a participant of this friendship");
        }
    }

    private FriendshipDto toDto(UUID me, CustomerFriendship f) {
        UUID otherId = me.equals(f.getCustomerA().getId()) ? f.getCustomerB().getId() : f.getCustomerA().getId();
        Customer other = me.equals(f.getCustomerA().getId()) ? f.getCustomerB() : f.getCustomerA();

        boolean requestedByMe = f.getRequestedBy().getId().equals(me);

        return new FriendshipDto(
                f.getId(),
                otherId,
                other.getUsername(),
                other.getDisplayName(),
                f.getStatus(),
                requestedByMe,
                f.getRespondedAt()
        );
    }
}
