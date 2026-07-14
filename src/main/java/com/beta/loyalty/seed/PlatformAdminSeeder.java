package com.beta.loyalty.seed;

import com.beta.loyalty.domain.Role;
import com.beta.loyalty.domain.StaffUser;
import com.beta.loyalty.domain.Tenant;
import com.beta.loyalty.domain.enums.StaffStatus;
import com.beta.loyalty.domain.enums.TenantStatus;
import com.beta.loyalty.repository.staff.RoleRepository;
import com.beta.loyalty.repository.staff.StaffUserRepository;
import com.beta.loyalty.repository.tenant.TenantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(0)
@RequiredArgsConstructor
public class PlatformAdminSeeder implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.platform.admin.email:admin@loyalty.platform}")
    private String adminEmail;

    @Value("${app.platform.admin.password:Platform123!}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant platform = tenantRepository.findByNameIgnoreCase("Platform")
                .orElseGet(() -> {
                    Tenant t = new Tenant();
                    t.setName("Platform");
                    t.setLegalName("Loyalty Platform");
                    t.setStatus(TenantStatus.ACTIVE);
                    return tenantRepository.save(t);
                });

        Role adminRole = roleRepository.findByTenantAndName(platform, "PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setTenant(platform);
                    r.setName("PLATFORM_ADMIN");
                    return roleRepository.save(r);
                });

        staffUserRepository.findByEmail(adminEmail).orElseGet(() -> {
            StaffUser admin = new StaffUser();
            admin.setTenant(platform);
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setStatus(StaffStatus.ACTIVE);
            admin.setRoles(Set.of(adminRole));
            StaffUser saved = staffUserRepository.save(admin);
            System.out.println("[SEED] Platform admin created: " + adminEmail);
            return saved;
        });
    }
}
