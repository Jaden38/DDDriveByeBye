package com.dddrivebye.ridemanagement.domain.entity;

public class PickedUpState implements RideState {
    @Override
    public void start(Ride ride) {
        ride.transitionTo(new InProgressState());
    }

    @Override
    public void reportIncident(Ride ride) {
        ride.transitionTo(new IncidentState());
    }
}
