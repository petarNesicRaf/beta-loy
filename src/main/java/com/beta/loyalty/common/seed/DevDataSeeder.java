package com.beta.loyalty.common.seed;

import com.beta.loyalty.auth.jwt.repo.CustomerAuthRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.CustomerStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds customer accounts for local development.
 *
 * Credentials:
 *   seeder@example.com         / Password123!
 *   secondseed@gmail.com       / Password123!
 *   ana.petrovic@example.com   / Password123!
 *   marko.jovic@example.com    / Password123!
 *   jelena.nikolic@example.com / Password123!
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final CustomerAuthRepository customerAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    @Value("${app.seed.customer.email:seeder@example.com}")
    private String seedEmail;

    @Value("${app.seed.customer.password:Password123!}")
    private String seedPassword;

    @Value("${app.seed.customer.display-name:seeder}")
    private String seedDisplayName;

    @Value("${app.seed.customer.username:seederUsername}")
    private String seedUsername;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        List.of(
            new CustomerSeed(seedEmail,                   seedPassword, seedDisplayName, seedUsername),
            new CustomerSeed("secondseed@gmail.com",       "Password123!", "User Two",    "user_two"),
            new CustomerSeed("ana.petrovic@example.com",   "Password123!", "Ana Petrovic","ana_petrovic"),
            new CustomerSeed("marko.jovic@example.com",    "Password123!", "Marko Jovic", "marko_jovic"),
            new CustomerSeed("jelena.nikolic@example.com", "Password123!", "Jelena Nikolic","jelena_nikolic")
        ).forEach(this::seedCustomer);
    }

    private void seedCustomer(CustomerSeed seed) {
        if (customerAuthRepository.findByEmailIgnoreCase(seed.email()).isPresent()) return;

        Customer c = new Customer();
        c.setEmail(seed.email());
        c.setDisplayName(seed.displayName());
        c.setUsername(seed.username());
        c.setStatus(CustomerStatus.ACTIVE);
        c.setPasswordHash(passwordEncoder.encode(seed.password()));
        customerAuthRepository.save(c);
        System.out.println("[SEED] Customer created: " + seed.email());
    }

    private record CustomerSeed(String email, String password, String displayName, String username) {}
}
