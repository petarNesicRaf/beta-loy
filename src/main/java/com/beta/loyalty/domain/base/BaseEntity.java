package com.beta.loyalty.domain.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    //super klasa koja samo ima uuid i kada je kreirana i updateovana
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    UUID id;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime createdAt;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
