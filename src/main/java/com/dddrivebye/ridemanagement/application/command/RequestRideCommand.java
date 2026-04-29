package com.dddrivebye.ridemanagement.application.command;

import java.util.UUID;

public record RequestRideCommand(
        UUID passengerId,
        double pickupLat,
        double pickupLon,
        double destLat,
        double destLon,
        int requestedSeats
) {
}
