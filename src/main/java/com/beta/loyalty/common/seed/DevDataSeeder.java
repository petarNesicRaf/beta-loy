package com.beta.loyalty.common.seed;

import com.beta.loyalty.auth.jwt.repo.CustomerAuthRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.CustomerStatus;
import com.beta.loyalty.friends.service.CustomerSearchService;
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

    private final CustomerAuthRepository customerAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerSearchService customerSearchService;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    @Value("${app.seed.customer.email:seeder@example.com}")
    private String seedEmail;

    @Value("${app.seed.customer.password:Password123!}")
    private String seedPassword;

    @Value("${app.seed.customer.display-name:seeder}")
    private String seedDisplayName;

    @Value("seederUsername")
    private String username;


    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        seedCustomer();
        // Later: seedTenants(); seedStaff(); seedRewards(); etc.
    }
    private void seedCustomer() {
        var existing = customerAuthRepository.findByEmailIgnoreCase(seedEmail);
        if (existing.isPresent()) return;

        Customer c = new Customer();
        c.setEmail(seedEmail);
        c.setDisplayName(seedDisplayName);
        c.setStatus(CustomerStatus.ACTIVE);
        c.setUsername(username);
        // For password-based testing only. OAuth customers can leave passwordHash null.
        c.setPasswordHash(passwordEncoder.encode(seedPassword));

        String seedSecondEmail = "secondseed@gmail.com";
        var existingSecond = customerAuthRepository.findByEmailIgnoreCase(seedSecondEmail);
        if (existingSecond.isPresent()) return;


        Customer c1 = new Customer();
        c1.setEmail(seedSecondEmail);
        c1.setDisplayName("user2");
        c1.setStatus(CustomerStatus.ACTIVE);
        c1.setUsername("secondSeeder");
        // For password-based testing only. OAuth customers can leave passwordHash null.
        c1.setPasswordHash(passwordEncoder.encode("secondPassword123"));


        customerAuthRepository.save(c);
        customerAuthRepository.save(c1);

        System.out.println("[SEED] Created customer: " + seedEmail);
    }
}
