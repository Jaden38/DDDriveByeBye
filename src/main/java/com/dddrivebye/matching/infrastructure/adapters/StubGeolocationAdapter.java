package com.dddrivebye.matching.infrastructure.adapters;

import com.dddrivebye.matching.application.port.GeolocationPort;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Placeholder adapter — to be replaced once the geolocation module exists.
 * Returns no nearby drivers so end-to-end calls succeed without Redis or
 * a routing provider; the matching engine then falls back to user-management's
 * available drivers list (provided by MatchingCommandHandler).
 */
@Component
@Profile("!geolocation-real")
public class StubGeolocationAdapter implements GeolocationPort {

    @Override
    public List<NearbyDriver> getDriversWithinRadius(GeoCoordinates point, double radiusKm) {
        return List.of();
    }

    @Override
    public double distanceKm(GeoCoordinates from, GeoCoordinates to) {
        return from.distanceKmTo(to);
    }
}
