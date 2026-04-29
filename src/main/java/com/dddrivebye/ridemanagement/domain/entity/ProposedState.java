package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public class ProposedState implements RideState {
    @Override
    public void accept(Ride ride) {
        ride.transitionTo(new AcceptedState());
    }

    @Override
    public void cancel(Ride ride) {
        ride.transitionTo(new CancelledState());
    }

    @Override
    public void propose(Ride ride, UserId driverId) {
        // Withdrawal or rejection - back to requested to find another one, or just update driver
        ride.setDriver(driverId);
        // stays in Proposed but for another driver
    }
}
