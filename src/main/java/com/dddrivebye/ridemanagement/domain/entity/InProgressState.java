package com.dddrivebye.ridemanagement.domain.entity;

public class InProgressState implements RideState {
    @Override
    public void arrive(Ride ride) {
        ride.transitionTo(new ArrivedState());
    }

    @Override
    public void reportIncident(Ride ride) {
        ride.transitionTo(new IncidentState());
    }
}
