package com.beta.loyalty.domain.customer;

import com.beta.loyalty.auth.oauth.OauthProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "customer_identities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerSubject"})
)
public class CustomerIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OauthProvider provider;

    @Column(nullable = false, length = 128)
    private String providerSubject; // Google "sub"

    private String email;

}
