package com.dddrivebye.geolocation.api.dto;

import java.time.Instant;
import java.util.UUID;

public record RouteDto(
        UUID id,
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude,
        double distanceKm,
        long durationMinutes,
        Instant calculatedAt
) {
}
