package com.dddrivebye.geolocation.infrastructure.persistence;

import com.dddrivebye.geolocation.domain.entity.Route;
import com.dddrivebye.geolocation.domain.repository.RouteRepository;
import com.dddrivebye.geolocation.domain.valueobject.RouteId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class JpaRouteRepository implements RouteRepository {

    private final SpringDataRouteRepository delegate;

    public JpaRouteRepository(SpringDataRouteRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Route route) {
        RouteJpaEntity entity = delegate.findById(route.id().value()).orElseGet(RouteJpaEntity::new);
        entity.setId(route.id().value());
        entity.setOriginLat(route.origin().latitude());
        entity.setOriginLon(route.origin().longitude());
        entity.setDestinationLat(route.destination().latitude());
        entity.setDestinationLon(route.destination().longitude());
        entity.setDistanceKm(route.distanceKm());
        entity.setDurationSeconds(route.duration().getSeconds());
        entity.setCalculatedAt(route.calculatedAt());
        delegate.save(entity);
    }

    @Override
    public Optional<Route> findById(RouteId id) {
        return delegate.findById(id.value()).map(this::toDomain);
    }

    private Route toDomain(RouteJpaEntity e) {
        return Route.reconstitute(
                RouteId.of(e.getId()),
                GeoCoordinates.of(e.getOriginLat(), e.getOriginLon()),
                GeoCoordinates.of(e.getDestinationLat(), e.getDestinationLon()),
                e.getDistanceKm(),
                Duration.ofSeconds(e.getDurationSeconds()),
                e.getCalculatedAt());
    }
}
