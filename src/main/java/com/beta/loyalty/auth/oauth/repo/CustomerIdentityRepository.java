package com.beta.loyalty.auth.oauth.repo;

import com.beta.loyalty.auth.oauth.OauthProvider;
import com.beta.loyalty.domain.customer.CustomerIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, Long> {
    Optional<CustomerIdentity> findByProviderAndProviderSubject(OauthProvider provider, String providerSubject);
}
