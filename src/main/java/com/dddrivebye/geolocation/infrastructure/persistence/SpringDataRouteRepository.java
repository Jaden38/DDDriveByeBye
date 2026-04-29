package com.dddrivebye.geolocation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataRouteRepository extends JpaRepository<RouteJpaEntity, UUID> {
}
