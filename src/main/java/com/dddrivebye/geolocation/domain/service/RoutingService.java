package com.dddrivebye.geolocation.domain.service;

import com.dddrivebye.geolocation.domain.valueobject.Eta;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.time.Duration;

/**
 * Outbound port to the third-party routing provider (Google Maps in production,
 * a deterministic mock in the current implementation phase).
 */
public interface RoutingService {

    RouteCalculation calculateRoute(GeoCoordinates origin, GeoCoordinates destination);

    Eta estimateEta(GeoCoordinates from, GeoCoordinates to);

    record RouteCalculation(double distanceKm, Duration duration) {
    }
}
