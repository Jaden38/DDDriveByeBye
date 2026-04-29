package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.exception.InvalidRideOperationException;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public interface RideState {
    
    default void propose(Ride ride, UserId driverId) {
        throw new InvalidRideOperationException("Cannot propose ride in state: " + this.getClass().getSimpleName());
    }

    default void accept(Ride ride) {
        throw new InvalidRideOperationException("Cannot accept ride in state: " + this.getClass().getSimpleName());
    }

    default void pickUp(Ride ride) {
        throw new InvalidRideOperationException("Cannot pick up passenger in state: " + this.getClass().getSimpleName());
    }

    default void start(Ride ride) {
        throw new InvalidRideOperationException("Cannot start ride in state: " + this.getClass().getSimpleName());
    }

    default void arrive(Ride ride) {
        throw new InvalidRideOperationException("Cannot arrive at destination in state: " + this.getClass().getSimpleName());
    }

    default void finalizeRide(Ride ride) {
        throw new InvalidRideOperationException("Cannot finalize ride in state: " + this.getClass().getSimpleName());
    }

    default void cancel(Ride ride) {
        throw new InvalidRideOperationException("Cannot cancel ride in state: " + this.getClass().getSimpleName());
    }

    default void reportIncident(Ride ride) {
        throw new InvalidRideOperationException("Cannot report incident in state: " + this.getClass().getSimpleName());
    }
}
