package com.dddrivebye.shared.domain.valueobject;

import java.util.Objects;

public final class GeoCoordinates {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final double latitude;
    private final double longitude;

    private GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GeoCoordinates of(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude out of range: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }
        return new GeoCoordinates(latitude, longitude);
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public double distanceKmTo(GeoCoordinates other) {
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoCoordinates that)) return false;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
