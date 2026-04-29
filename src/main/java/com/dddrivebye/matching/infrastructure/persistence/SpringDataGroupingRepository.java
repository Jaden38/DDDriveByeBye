package com.dddrivebye.matching.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataGroupingRepository extends JpaRepository<GroupingJpaEntity, UUID> {

    @Query("""
            select g from GroupingJpaEntity g
            join g.members m
            where m.rideRequestId = :rideRequestId
              and g.status <> 'DISSOLVED'
            """)
    Optional<GroupingJpaEntity> findActiveByRideRequestId(@Param("rideRequestId") UUID rideRequestId);
}
