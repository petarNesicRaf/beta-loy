package com.beta.loyalty.friends.repository;

import com.beta.loyalty.domain.customer.CustomerFriendship;
import com.beta.loyalty.domain.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerFriendshipRepository extends JpaRepository<CustomerFriendship, UUID> {
    Optional<CustomerFriendship> findByCustomerAIdAndCustomerBId(UUID aId, UUID bId);
    @Query("""
      select f from CustomerFriendship f
      where (f.customerA.id = :me or f.customerB.id = :me)
        and f.status = :status
    """)
    Page<CustomerFriendship> findMineByStatus(@Param("me") UUID me,
                                              @Param("status") FriendshipStatus status,
                                              Pageable pageable);

    @Query("""
        select f from CustomerFriendship f
        where f.status = com.beta.loyalty.domain.enums.FriendshipStatus.PENDING
          and f.requestedBy.id <> :me
          and (f.customerA.id = :me or f.customerB.id = :me)
    """)
    Page<CustomerFriendship> findIncomingPending(@Param("me") UUID me, Pageable pageable);

    @Query("""
        select f from CustomerFriendship f
        where f.status = com.beta.loyalty.domain.enums.FriendshipStatus.PENDING
          and f.requestedBy.id = :me
          and (f.customerA.id = :me or f.customerB.id = :me)
    """)
    Page<CustomerFriendship> findOutgoingPending(@Param("me") UUID me, Pageable pageable);

    // Helpful for search "relationship flags" (bulk)
    @Query("""
        select f from CustomerFriendship f
        where (f.customerA.id = :me and f.customerB.id in :others)
           or (f.customerB.id = :me and f.customerA.id in :others)
    """)
    List<CustomerFriendship> findBetweenMeAndOthers(@Param("me") UUID me,
                                                    @Param("others") Collection<UUID> others);

}
