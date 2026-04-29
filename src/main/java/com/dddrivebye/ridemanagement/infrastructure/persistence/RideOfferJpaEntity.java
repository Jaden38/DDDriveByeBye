package com.dddrivebye.ridemanagement.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "ride", name = "ride_offers")
@Getter
@Setter
public class RideOfferJpaEntity {
    @Id
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "pickup_lat", nullable = false)
    private double pickupLat;

    @Column(name = "pickup_lon", nullable = false)
    private double pickupLon;

    @Column(name = "dest_lat", nullable = false)
    private double destLat;

    @Column(name = "dest_lon", nullable = false)
    private double destLon;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "price_amount")
    private BigDecimal priceAmount;

    @Column(name = "price_currency")
    private String priceCurrency;

    @Column(name = "active", nullable = false)
    private boolean active;
}
