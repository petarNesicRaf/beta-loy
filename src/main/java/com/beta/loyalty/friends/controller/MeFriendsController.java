package com.beta.loyalty.friends.controller;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.friends.dto.CustomerSearchItemDto;
import com.beta.loyalty.friends.dto.FriendRequestCreateDto;
import com.beta.loyalty.friends.dto.FriendshipDto;
import com.beta.loyalty.friends.service.CustomerSearchService;
import com.beta.loyalty.friends.service.FriendshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/me/friends")
@Tag(
        name = "Friends",
        description = "Sve operacije vezane za prijatelje trenutnog user-a."
)
public class MeFriendsController {

    private final FriendshipService friendshipService;
    private final CustomerSearchService customerSearchService;

    @GetMapping("/search")
    public Page<CustomerSearchItemDto> searchCustomers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID me = CurrentUser.requirePrincipal().userId();
        return customerSearchService.searchByUsername(me, q, pageable);
    }

    //todo zameni extractcustomerid stavi ga u servise
    @PostMapping("/requests")
    public FriendshipDto sendRequest(
            @AuthenticationPrincipal Object principal,
            @RequestBody FriendRequestCreateDto body
    ) {
        UUID me = extractCustomerId();
        return friendshipService.sendRequest(me, body.targetCustomerId());
    }

    @PostMapping("/requests/{friendshipId}/accept")
    public FriendshipDto accept(
            @AuthenticationPrincipal Object principal,
            @PathVariable UUID friendshipId
    ) {
        UUID me = extractCustomerId();
        return friendshipService.accept(me, friendshipId);
    }

    @PostMapping("/requests/{friendshipId}/decline")
    public FriendshipDto decline(
            @PathVariable UUID friendshipId
    ) {
        UUID me = extractCustomerId();
        return friendshipService.decline(me, friendshipId);
    }

    @PostMapping("/requests/{friendshipId}/cancel")
    public FriendshipDto cancel(
            @PathVariable UUID friendshipId
    ) {
        UUID me = extractCustomerId();
        return friendshipService.cancel(me, friendshipId);
    }

    @GetMapping("/accepted")
    public Page<FriendshipDto> listAccepted(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID me = extractCustomerId();
        return friendshipService.listAccepted(me, pageable);
    }

    @GetMapping("/requests/incoming")
    public Page<FriendshipDto> incoming(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID me = extractCustomerId();
        return friendshipService.listIncomingPending(me, pageable);
    }

    @GetMapping("/requests/outgoing")
    public Page<FriendshipDto> outgoing(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID me = extractCustomerId();
        return friendshipService.listOutgoingPending(me, pageable);
    }

    private UUID extractCustomerId() {
        return CurrentUser.requirePrincipal().userId();
    }
}
