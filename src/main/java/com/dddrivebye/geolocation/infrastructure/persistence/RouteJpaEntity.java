package com.dddrivebye.geolocation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "geo", name = "routes")
public class RouteJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "origin_lat", nullable = false)
    private double originLat;

    @Column(name = "origin_lon", nullable = false)
    private double originLon;

    @Column(name = "destination_lat", nullable = false)
    private double destinationLat;

    @Column(name = "destination_lon", nullable = false)
    private double destinationLon;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public double getOriginLat() { return originLat; }
    public void setOriginLat(double originLat) { this.originLat = originLat; }
    public double getOriginLon() { return originLon; }
    public void setOriginLon(double originLon) { this.originLon = originLon; }
    public double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(double destinationLat) { this.destinationLat = destinationLat; }
    public double getDestinationLon() { return destinationLon; }
    public void setDestinationLon(double destinationLon) { this.destinationLon = destinationLon; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
