package com.dddrivebye.geolocation.application.command;

public record CalculateRouteCommand(
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude
) {
}
