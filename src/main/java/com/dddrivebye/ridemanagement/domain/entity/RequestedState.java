package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public class RequestedState implements RideState {
    @Override
    public void propose(Ride ride, UserId driverId) {
        ride.setDriver(driverId);
        ride.transitionTo(new ProposedState());
    }

    @Override
    public void cancel(Ride ride) {
        ride.transitionTo(new CancelledState());
    }
}
