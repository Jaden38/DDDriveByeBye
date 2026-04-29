package com.dddrivebye.matching.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataMatchRepository extends JpaRepository<MatchJpaEntity, UUID> {

    Optional<MatchJpaEntity> findByRideId(UUID rideId);
}
