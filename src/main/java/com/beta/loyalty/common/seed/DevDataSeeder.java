package com.beta.loyalty.common.seed;

import com.beta.loyalty.auth.jwt.repo.CustomerRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.CustomerStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    @Value("${app.seed.customer.email:seeder@example.com}")
    private String seedEmail;

    @Value("${app.seed.customer.password:Password123!}")
    private String seedPassword;

    @Value("${app.seed.customer.display-name:seeder}")
    private String seedDisplayName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        seedCustomer();
        // Later: seedTenants(); seedStaff(); seedRewards(); etc.
    }
    private void seedCustomer() {
        var existing = customerRepository.findByEmailIgnoreCase(seedEmail);
        if (existing.isPresent()) return;

        Customer c = new Customer();
        c.setEmail(seedEmail);
        c.setDisplayName(seedDisplayName);
        c.setStatus(CustomerStatus.ACTIVE);

        // For password-based testing only. OAuth customers can leave passwordHash null.
        c.setPasswordHash(passwordEncoder.encode(seedPassword));

        customerRepository.save(c);

        System.out.println("[SEED] Created customer: " + seedEmail);
    }
}
