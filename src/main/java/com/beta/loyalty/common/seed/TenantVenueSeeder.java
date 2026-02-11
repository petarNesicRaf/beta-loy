package com.beta.loyalty.common.seed;

import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.domain.enums.TenantStatus;
import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.domain.reward.Reward;
import com.beta.loyalty.domain.tenant.Tenant;
import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.rewards.rewards.RewardRepository;
import com.beta.loyalty.tenants.repository.TenantRepository;
import com.beta.loyalty.venues.repository.VenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TenantVenueSeeder implements ApplicationRunner {
    private final TenantRepository tenantRepository;
    private final VenueRepository venueRepository;
    private final RewardRepository rewardRepository;
    @Value("${app.seed.enabled:false}")
    private boolean enabled;


    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        Tenant dockerTenant = seedTenant(
                "Docker Corp",
                "docker Corp LLC",
                "100200300"
        );

        seedVenue(dockerTenant, "Docker Dorcol", "Main St 12", "Belgrade", dockerTenant.getPib(), VenueType.DRINKS);
        seedVenue(dockerTenant, "Docker NBG", "Mall Blvd 5", "Belgrade", dockerTenant.getPib(),  VenueType.DRINKS);

        Tenant saintGroup = seedTenant(
                "Saint Tenant",
                "Saint Group DOO",
                "99887766"
        );

        seedVenue(saintGroup, "Saint Stadion", "Square 1", "Novi Sad", saintGroup.getPib(),  VenueType.FAST_FOOD);
        seedVenue(saintGroup, "Saint Bulevar", "West Ave 42", "Novi Sad", saintGroup.getPib(),  VenueType.FAST_FOOD);


        List<Venue> venues = venueRepository.findAll();

        for (Venue venue : venues) {
            // avoid duplicating seeds
            if (rewardRepository.existsByVenueId(venue.getId())) {
                continue;
            }

            rewardRepository.saveAll(List.of(
                    reward(venue, "Free espresso", "Get 1 free espresso with any purchase.", 120, 200, 2),
                    reward(venue, "10% off bill", "10% discount on the total bill.", 250, 100, 1),
                    reward(venue, "Free dessert", "Free dessert with a main course.", 400, 50, 1)
            ));
        }
    }

    private Tenant seedTenant(String name, String legalName, String pib) {
        return tenantRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Tenant t = new Tenant();
                    t.setName(name);
                    t.setLegalName(legalName);
                    t.setPib(pib);
                    t.setStatus(TenantStatus.ACTIVE);

                    Tenant saved = tenantRepository.save(t);
                    System.out.println("[SEED] Tenant created: " + name);
                    return saved;
                });
    }

    private void seedVenue(Tenant tenant, String name, String address, String city, String pib, VenueType type) {
        venueRepository.findByTenantAndNameIgnoreCase(tenant, name)
                .orElseGet(() -> {
                    Venue v = new Venue();
                    v.setTenant(tenant);
                    v.setPib(pib);
                    v.setName(name);
                    v.setAddress(address);
                    v.setCity(city);
                    v.setCountry("RS");
                    v.setTimezone("Europe/Belgrade");
                    v.setStatus(VenueStatus.ACTIVE);
                    v.setType(type);

                    Venue saved = venueRepository.save(v);
                    System.out.println(
                            "[SEED] Venue created: " + name +
                                    " (tenant=" + tenant.getName() + ")"
                    );
                    return saved;
                });
    }
    private Reward reward(Venue venue, String name, String desc, long cost, Integer stock, Integer perDayLimit) {
        Reward r = new Reward();
        r.setVenue(venue);
        r.setName(name);
        r.setDescription(desc);
        r.setPointsCost(cost);
        r.setStatus(RewardStatus.ACTIVE);

        // optional validity window
        r.setValidFrom(OffsetDateTime.now().minusDays(1));
        r.setValidTo(OffsetDateTime.now().plusMonths(3));

        r.setStock(stock);
        r.setPerCustomerLimitPerDay(perDayLimit);
        return r;
    }
}
