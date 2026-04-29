package com.dddrivebye.ridemanagement.domain.exception;

import com.dddrivebye.ridemanagement.domain.valueobject.RideId;

public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException(RideId id) {
        super("Ride not found: " + id.value());
    }
}
