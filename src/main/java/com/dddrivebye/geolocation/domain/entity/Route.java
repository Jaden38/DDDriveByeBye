package com.dddrivebye.geolocation.domain.entity;

import com.dddrivebye.geolocation.domain.valueobject.RouteId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Persistent geographic path between an origin and a destination,
 * with the calculated distance and expected duration. The actual path
 * geometry is owned by the routing provider (mocked here).
 */
public final class Route {

    private final RouteId id;
    private final GeoCoordinates origin;
    private final GeoCoordinates destination;
    private final double distanceKm;
    private final Duration duration;
    private final Instant calculatedAt;

    private Route(RouteId id,
                  GeoCoordinates origin,
                  GeoCoordinates destination,
                  double distanceKm,
                  Duration duration,
                  Instant calculatedAt) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.duration = duration;
        this.calculatedAt = calculatedAt;
    }

    public static Route create(GeoCoordinates origin,
                               GeoCoordinates destination,
                               double distanceKm,
                               Duration duration) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative: " + distanceKm);
        }
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be non-negative");
        }
        return new Route(RouteId.generate(), origin, destination, distanceKm, duration, Instant.now());
    }

    public static Route reconstitute(RouteId id,
                                     GeoCoordinates origin,
                                     GeoCoordinates destination,
                                     double distanceKm,
                                     Duration duration,
                                     Instant calculatedAt) {
        return new Route(id, origin, destination, distanceKm, duration, calculatedAt);
    }

    public RouteId id() {
        return id;
    }

    public GeoCoordinates origin() {
        return origin;
    }

    public GeoCoordinates destination() {
        return destination;
    }

    public double distanceKm() {
        return distanceKm;
    }

    public Duration duration() {
        return duration;
    }

    public Instant calculatedAt() {
        return calculatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
