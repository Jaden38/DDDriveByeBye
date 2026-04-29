package com.dddrivebye.territorialconfiguration.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "territory", name = "territories")
public class TerritoryJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "center_lat", nullable = false)
    private double centerLat;

    @Column(name = "center_lon", nullable = false)
    private double centerLon;

    @Column(name = "radius_km", nullable = false)
    private double radiusKm;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "per_km_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal perKmRate;

    @Column(name = "per_min_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal perMinRate;

    @Column(name = "pickup_fee", nullable = false, precision = 10, scale = 4)
    private BigDecimal pickupFee;

    @Column(name = "max_surge_coefficient", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxSurgeCoefficient;

    @Column(name = "cancellation_fee", nullable = false, precision = 10, scale = 4)
    private BigDecimal cancellationFee;

    @Column(name = "carpooling_grouping_enabled", nullable = false)
    private boolean carpoolingGroupingEnabled;

    @Column(name = "fixed_fare", precision = 10, scale = 4)
    private BigDecimal fixedFare;

    @OneToMany(mappedBy = "territory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TerritoryConstraintJpaEntity> constraints = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCenterLat() { return centerLat; }
    public void setCenterLat(double centerLat) { this.centerLat = centerLat; }
    public double getCenterLon() { return centerLon; }
    public void setCenterLon(double centerLon) { this.centerLon = centerLon; }
    public double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getPerKmRate() { return perKmRate; }
    public void setPerKmRate(BigDecimal perKmRate) { this.perKmRate = perKmRate; }
    public BigDecimal getPerMinRate() { return perMinRate; }
    public void setPerMinRate(BigDecimal perMinRate) { this.perMinRate = perMinRate; }
    public BigDecimal getPickupFee() { return pickupFee; }
    public void setPickupFee(BigDecimal pickupFee) { this.pickupFee = pickupFee; }
    public BigDecimal getMaxSurgeCoefficient() { return maxSurgeCoefficient; }
    public void setMaxSurgeCoefficient(BigDecimal maxSurgeCoefficient) { this.maxSurgeCoefficient = maxSurgeCoefficient; }
    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }
    public boolean isCarpoolingGroupingEnabled() { return carpoolingGroupingEnabled; }
    public void setCarpoolingGroupingEnabled(boolean carpoolingGroupingEnabled) { this.carpoolingGroupingEnabled = carpoolingGroupingEnabled; }
    public BigDecimal getFixedFare() { return fixedFare; }
    public void setFixedFare(BigDecimal fixedFare) { this.fixedFare = fixedFare; }
    public List<TerritoryConstraintJpaEntity> getConstraints() { return constraints; }
    public void setConstraints(List<TerritoryConstraintJpaEntity> constraints) { this.constraints = constraints; }
}
