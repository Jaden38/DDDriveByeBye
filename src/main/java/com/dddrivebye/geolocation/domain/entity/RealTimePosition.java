package com.dddrivebye.geolocation.domain.entity;

import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.time.Instant;
import java.util.Objects;

/**
 * Snapshot of a driver's current real-time geographic position.
 * Stored ephemerally in Redis with a short TTL.
 */
public final class RealTimePosition {

    private final DriverId driverId;
    private final GeoCoordinates coordinates;
    private final Instant capturedAt;

    private RealTimePosition(DriverId driverId, GeoCoordinates coordinates, Instant capturedAt) {
        this.driverId = driverId;
        this.coordinates = coordinates;
        this.capturedAt = capturedAt;
    }

    public static RealTimePosition capture(DriverId driverId, GeoCoordinates coordinates) {
        return new RealTimePosition(driverId, coordinates, Instant.now());
    }

    public static RealTimePosition reconstitute(DriverId driverId,
                                                GeoCoordinates coordinates,
                                                Instant capturedAt) {
        return new RealTimePosition(driverId, coordinates, capturedAt);
    }

    public DriverId driverId() {
        return driverId;
    }

    public GeoCoordinates coordinates() {
        return coordinates;
    }

    public Instant capturedAt() {
        return capturedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealTimePosition that)) return false;
        return driverId.equals(that.driverId)
                && coordinates.equals(that.coordinates)
                && capturedAt.equals(that.capturedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId, coordinates, capturedAt);
    }
}
