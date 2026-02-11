package com.beta.loyalty.points.repository;

import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.points.PointsLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointsLedgerEntryRepository extends JpaRepository<PointsLedgerEntry, UUID> {
    boolean existsBySourceTypeAndSourceIdAndCustomerId(LedgerSourceType sourceType, UUID sourceId, UUID customerId);

}
