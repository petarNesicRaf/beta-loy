package com.beta.loyalty.points.repository;

import com.beta.loyalty.domain.points.PointsAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PointsAccountRepository extends JpaRepository<PointsAccount, UUID> {
    Optional<PointsAccount> findByCustomerIdAndVenueId(UUID customerId, UUID venueId);
    @Query("""
        SELECT pa.currentBalance
        FROM PointsAccount pa
        WHERE pa.customer.id = :customerId
          AND pa.venue.id = :venueId
    """)
    Optional<Long> findBalance(
            @Param("customerId") UUID customerId,
            @Param("venueId") UUID venueId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pa FROM PointsAccount pa
        WHERE pa.customer.id = :customerId AND pa.venue.id = :venueId
    """)
    Optional<PointsAccount> findForUpdate(@Param("customerId") UUID customerId,
                                          @Param("venueId") UUID venueId);
}
