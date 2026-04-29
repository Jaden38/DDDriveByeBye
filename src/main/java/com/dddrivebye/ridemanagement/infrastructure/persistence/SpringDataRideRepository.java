package com.dddrivebye.ridemanagement.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataRideRepository extends JpaRepository<RideJpaEntity, UUID> {
}
