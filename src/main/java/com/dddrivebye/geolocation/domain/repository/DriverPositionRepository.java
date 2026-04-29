package com.dddrivebye.geolocation.domain.repository;

import com.dddrivebye.geolocation.domain.entity.RealTimePosition;
import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.util.List;
import java.util.Optional;

/**
 * Real-time driver position storage. Backed by Redis GEO commands —
 * `geo:available-drivers` (GEO set) + `geo:driver:{driverId}:position` (TTL 30s).
 */
public interface DriverPositionRepository {

    void save(RealTimePosition position);

    Optional<RealTimePosition> findByDriverId(DriverId driverId);

    void remove(DriverId driverId);

    List<DriverWithDistance> findWithinRadius(GeoCoordinates center, double radiusKm);

    record DriverWithDistance(DriverId driverId,
                              GeoCoordinates coordinates,
                              double distanceKm) {
    }
}
