package com.beta.loyalty.common.seed;

import com.beta.loyalty.auth.jwt.repo.CustomerAuthRepository;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.tenant.Tenant;
import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.points.service.PointsService;
import com.beta.loyalty.tenants.repository.TenantRepository;
import com.beta.loyalty.venues.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Seeds points balances so dev/test scenarios are immediately usable.
 *
 * Uses MANUAL_ADJUSTMENT ledger entries with deterministic sourceIds
 * (UUID v3 from a stable string), so re-running the seeder is idempotent.
 *
 * Balances after seeding:
 *
 *  Customer                    Venue            Points
 *  seeder@example.com          Docker Dorcol     500
 *  seeder@example.com          Saint Stadion     300
 *  secondseed@gmail.com        Docker Dorcol     350
 *  secondseed@gmail.com        Docker NBG        180
 *  ana.petrovic@example.com    Docker NBG        620
 *  ana.petrovic@example.com    Saint Bulevar     410
 *  marko.jovic@example.com     Saint Bulevar     150
 *  marko.jovic@example.com     Saint Stadion     280
 *  jelena.nikolic@example.com  Docker Dorcol     800
 *  jelena.nikolic@example.com  Saint Stadion     200
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class PointsSeeder implements ApplicationRunner {

    private final CustomerAuthRepository customerAuthRepository;
    private final TenantRepository tenantRepository;
    private final VenueRepository venueRepository;
    private final PointsService pointsService;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        Tenant docker = tenantRepository.findByNameIgnoreCase("Docker Corp").orElse(null);
        Tenant saint  = tenantRepository.findByNameIgnoreCase("Saint Tenant").orElse(null);
        if (docker == null || saint == null) return;

        Optional<Venue> dockerDorcol  = venueRepository.findByTenantAndNameIgnoreCase(docker, "Docker Dorcol");
        Optional<Venue> dockerNbg     = venueRepository.findByTenantAndNameIgnoreCase(docker, "Docker NBG");
        Optional<Venue> saintStadion  = venueRepository.findByTenantAndNameIgnoreCase(saint,  "Saint Stadion");
        Optional<Venue> saintBulevar  = venueRepository.findByTenantAndNameIgnoreCase(saint,  "Saint Bulevar");

        List.of(
            new PointsSeed("seeder@example.com",          dockerDorcol,  500),
            new PointsSeed("seeder@example.com",          saintStadion,  300),
            new PointsSeed("secondseed@gmail.com",        dockerDorcol,  350),
            new PointsSeed("secondseed@gmail.com",        dockerNbg,     180),
            new PointsSeed("ana.petrovic@example.com",    dockerNbg,     620),
            new PointsSeed("ana.petrovic@example.com",    saintBulevar,  410),
            new PointsSeed("marko.jovic@example.com",     saintBulevar,  150),
            new PointsSeed("marko.jovic@example.com",     saintStadion,  280),
            new PointsSeed("jelena.nikolic@example.com",  dockerDorcol,  800),
            new PointsSeed("jelena.nikolic@example.com",  saintStadion,  200)
        ).forEach(this::applyPoints);
    }

    private void applyPoints(PointsSeed seed) {
        if (seed.venue().isEmpty()) return;

        customerAuthRepository.findByEmailIgnoreCase(seed.email()).ifPresent(customer -> {
            Venue venue = seed.venue().get();

            // Deterministic sourceId — same string always produces the same UUID,
            // so the ledger idempotency guard prevents double-awarding on re-run.
            UUID sourceId = UUID.nameUUIDFromBytes(
                    ("seed:" + customer.getId() + ":" + venue.getId())
                            .getBytes(StandardCharsets.UTF_8)
            );

            pointsService.earn(
                    customer.getId(),
                    venue.getId(),
                    seed.points(),
                    LedgerSourceType.MANUAL_ADJUSTMENT,
                    sourceId
            );

            System.out.println("[SEED] Points awarded: " + seed.points()
                    + " → " + seed.email() + " @ " + venue.getName());
        });
    }

    private record PointsSeed(String email, Optional<Venue> venue, long points) {}
}