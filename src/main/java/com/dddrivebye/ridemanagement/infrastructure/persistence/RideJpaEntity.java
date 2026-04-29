package com.dddrivebye.ridemanagement.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "ride", name = "rides")
@Getter
@Setter
public class RideJpaEntity {
    @Id
    private UUID id;

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "pickup_lat", nullable = false)
    private double pickupLat;

    @Column(name = "pickup_lon", nullable = false)
    private double pickupLon;

    @Column(name = "dest_lat", nullable = false)
    private double destLat;

    @Column(name = "dest_lon", nullable = false)
    private double destLon;

    @Column(name = "price_amount")
    private BigDecimal priceAmount;

    @Column(name = "price_currency")
    private String priceCurrency;

    @Column(name = "requested_seats", nullable = false)
    private int requestedSeats;
}
