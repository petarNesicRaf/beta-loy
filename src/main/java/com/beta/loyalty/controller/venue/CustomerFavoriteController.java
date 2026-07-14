package com.beta.loyalty.controller.venue;

import com.beta.loyalty.dto.venue.VenueFavoriteResponse;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.venue.VenueFavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/favorites")
@Tag(name = "Customer - Favorites", description = "Bookmark venues and see your balance at each one")
public class CustomerFavoriteController {

    private final VenueFavoriteService venueFavoriteService;

    @GetMapping
    public List<VenueFavoriteResponse> list() {
        return venueFavoriteService.listFavorites(CurrentUser.requirePrincipal().userId());
    }

    @PostMapping("/{venueId}")
    @ResponseStatus(HttpStatus.CREATED)
    public VenueFavoriteResponse add(@PathVariable UUID venueId) {
        return venueFavoriteService.addFavorite(CurrentUser.requirePrincipal().userId(), venueId);
    }

    @DeleteMapping("/{venueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID venueId) {
        venueFavoriteService.removeFavorite(CurrentUser.requirePrincipal().userId(), venueId);
    }
}
