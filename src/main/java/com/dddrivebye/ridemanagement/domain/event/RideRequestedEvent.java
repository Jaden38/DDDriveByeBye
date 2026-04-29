package com.dddrivebye.ridemanagement.domain.event;

import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public final class RideRequestedEvent extends BaseDomainEvent {

    private final RideId rideId;
    private final UserId passengerId;
    private final GeoCoordinates pickupPoint;
    private final GeoCoordinates destination;
    private final int requestedSeats;

    public RideRequestedEvent(RideId rideId,
                              UserId passengerId,
                              GeoCoordinates pickupPoint,
                              GeoCoordinates destination,
                              int requestedSeats) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.pickupPoint = pickupPoint;
        this.destination = destination;
        this.requestedSeats = requestedSeats;
    }

    public RideId rideId() {
        return rideId;
    }

    public UserId passengerId() {
        return passengerId;
    }

    public GeoCoordinates pickupPoint() {
        return pickupPoint;
    }

    public GeoCoordinates destination() {
        return destination;
    }

    public int requestedSeats() {
        return requestedSeats;
    }
}
