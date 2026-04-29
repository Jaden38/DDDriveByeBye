package com.dddrivebye.matching.application.port;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.util.List;
import java.util.UUID;

/**
 * Port describing what the matching module needs from the geolocation
 * bounded context. Until the geolocation module is implemented (Step 2),
 * a stub adapter satisfies this contract.
 */
public interface GeolocationPort {

    List<NearbyDriver> getDriversWithinRadius(GeoCoordinates point, double radiusKm);

    double distanceKm(GeoCoordinates from, GeoCoordinates to);

    record NearbyDriver(UUID driverId, GeoCoordinates position, double distanceKm) {
    }
}
