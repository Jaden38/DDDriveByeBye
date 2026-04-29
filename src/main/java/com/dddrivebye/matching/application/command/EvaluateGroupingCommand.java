package com.dddrivebye.matching.application.command;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.util.List;
import java.util.UUID;

public record EvaluateGroupingCommand(
        UUID driverId,
        int driverSeatsAvailable,
        List<RideRequestCandidate> candidates,
        double pickupProximityKm,
        boolean territoryCarpoolingEnabled
) {

    public record RideRequestCandidate(
            UUID rideRequestId,
            UUID passengerId,
            GeoCoordinates pickup,
            GeoCoordinates destination,
            boolean carpoolingOptIn
    ) {
    }
}
