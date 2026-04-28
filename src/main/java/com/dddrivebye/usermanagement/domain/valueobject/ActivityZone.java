package com.dddrivebye.usermanagement.domain.valueobject;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.util.Objects;

public final class ActivityZone {

    private final String label;
    private final GeoCoordinates center;
    private final double radiusKm;

    private ActivityZone(String label, GeoCoordinates center, double radiusKm) {
        this.label = label;
        this.center = center;
        this.radiusKm = radiusKm;
    }

    public static ActivityZone of(String label, GeoCoordinates center, double radiusKm) {
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(center, "center must not be null");
        if (label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm must be positive: " + radiusKm);
        }
        return new ActivityZone(label, center, radiusKm);
    }

    public String label() {
        return label;
    }

    public GeoCoordinates center() {
        return center;
    }

    public double radiusKm() {
        return radiusKm;
    }

    public boolean covers(GeoCoordinates point) {
        return center.distanceKmTo(point) <= radiusKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActivityZone that)) return false;
        return Double.compare(that.radiusKm, radiusKm) == 0
                && label.equals(that.label)
                && center.equals(that.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, center, radiusKm);
    }
}
