package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.event.RideFinalizedEvent;

public class ArrivedState implements RideState {
    @Override
    public void finalizeRide(Ride ride) {
        ride.transitionTo(new FinalizedState());
        ride.addEvent(new RideFinalizedEvent(ride.id()));
    }
}
