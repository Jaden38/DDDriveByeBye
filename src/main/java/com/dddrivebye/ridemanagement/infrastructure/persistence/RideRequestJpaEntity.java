package com.dddrivebye.ridemanagement.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "ride", name = "ride_requests")
@Getter
@Setter
public class RideRequestJpaEntity {
    @Id
    private UUID id;

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Column(name = "pickup_lat", nullable = false)
    private double pickupLat;

    @Column(name = "pickup_lon", nullable = false)
    private double pickupLon;

    @Column(name = "dest_lat", nullable = false)
    private double destLat;

    @Column(name = "dest_lon", nullable = false)
    private double destLon;

    @Column(name = "requested_time", nullable = false)
    private LocalDateTime requestedTime;

    @Column(name = "requested_seats", nullable = false)
    private int requestedSeats;

    @Column(name = "fulfilled", nullable = false)
    private boolean fulfilled;
}
