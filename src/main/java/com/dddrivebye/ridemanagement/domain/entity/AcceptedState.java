package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.event.RideAcceptedEvent;

public class AcceptedState implements RideState {
    @Override
    public void pickUp(Ride ride) {
        ride.transitionTo(new PickedUpState());
    }

    @Override
    public void cancel(Ride ride) {
        ride.transitionTo(new CancelledState());
    }
}
