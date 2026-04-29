package com.dddrivebye.geolocation.infrastructure.adapter;

import com.dddrivebye.geolocation.domain.service.RoutingService;
import com.dddrivebye.geolocation.domain.valueobject.Eta;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Deterministic stand-in for the Google Maps routing API.
 * Distance = haversine straight line; duration = distance / 30 km/h average.
 * ETA is fixed at 5 minutes per the spec.
 */
@Component
public class MockRoutingAdapter implements RoutingService {

    private static final double AVERAGE_SPEED_KMH = 30.0;
    private static final long FIXED_ETA_MINUTES = 5;

    @Override
    public RouteCalculation calculateRoute(GeoCoordinates origin, GeoCoordinates destination) {
        double distanceKm = origin.distanceKmTo(destination);
        long durationMinutes = Math.max(1, Math.round(distanceKm / AVERAGE_SPEED_KMH * 60));
        return new RouteCalculation(distanceKm, Duration.ofMinutes(durationMinutes));
    }

    @Override
    public Eta estimateEta(GeoCoordinates from, GeoCoordinates to) {
        return Eta.ofMinutes(FIXED_ETA_MINUTES);
    }
}
