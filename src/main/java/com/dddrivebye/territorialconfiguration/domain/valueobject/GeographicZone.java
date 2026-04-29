package com.dddrivebye.territorialconfiguration.domain.valueobject;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.util.Objects;

public final class GeographicZone {

    private final GeoCoordinates center;
    private final double radiusKm;

    private GeographicZone(GeoCoordinates center, double radiusKm) {
        this.center = center;
        this.radiusKm = radiusKm;
    }

    public static GeographicZone of(GeoCoordinates center, double radiusKm) {
        Objects.requireNonNull(center);
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm must be positive: " + radiusKm);
        }
        return new GeographicZone(center, radiusKm);
    }

    public GeoCoordinates center() {
        return center;
    }

    public double radiusKm() {
        return radiusKm;
    }

    public double areaKm2() {
        return Math.PI * radiusKm * radiusKm;
    }

    public boolean covers(GeoCoordinates point) {
        return center.distanceKmTo(point) <= radiusKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeographicZone that)) return false;
        return Double.compare(that.radiusKm, radiusKm) == 0 && center.equals(that.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radiusKm);
    }
}
