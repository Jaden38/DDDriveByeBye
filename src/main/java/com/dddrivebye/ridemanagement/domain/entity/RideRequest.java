package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.valueobject.RideRequestId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

import java.time.LocalDateTime;

public class RideRequest {
    private final RideRequestId id;
    private final UserId passengerId;
    private final GeoCoordinates pickupPoint;
    private final GeoCoordinates destination;
    private final LocalDateTime requestedTime;
    private final int requestedSeats;
    private boolean fulfilled;

    private RideRequest(RideRequestId id, UserId passengerId, GeoCoordinates pickupPoint, 
                        GeoCoordinates destination, LocalDateTime requestedTime, int requestedSeats, boolean fulfilled) {
        this.id = id;
        this.passengerId = passengerId;
        this.pickupPoint = pickupPoint;
        this.destination = destination;
        this.requestedTime = requestedTime;
        this.requestedSeats = requestedSeats;
        this.fulfilled = fulfilled;
    }

    public static RideRequest create(UserId passengerId, GeoCoordinates pickupPoint, GeoCoordinates destination, 
                                     LocalDateTime requestedTime, int requestedSeats) {
        return new RideRequest(RideRequestId.generate(), passengerId, pickupPoint, destination, requestedTime, requestedSeats, false);
    }

    public static RideRequest reconstitute(RideRequestId id, UserId passengerId, GeoCoordinates pickupPoint, 
                                            GeoCoordinates destination, LocalDateTime requestedTime, 
                                            int requestedSeats, boolean fulfilled) {
        return new RideRequest(id, passengerId, pickupPoint, destination, requestedTime, requestedSeats, fulfilled);
    }

    public void fulfill() {
        this.fulfilled = true;
    }

    public RideRequestId id() { return id; }
    public UserId passengerId() { return passengerId; }
    public GeoCoordinates pickupPoint() { return pickupPoint; }
    public GeoCoordinates destination() { return destination; }
    public LocalDateTime requestedTime() { return requestedTime; }
    public int requestedSeats() { return requestedSeats; }
    public boolean isFulfilled() { return fulfilled; }
}
