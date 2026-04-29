package com.dddrivebye.geolocation.api.dto;

import java.time.Instant;
import java.util.UUID;

public record DriverPositionDto(
        UUID driverId,
        double latitude,
        double longitude,
        Instant capturedAt
) {
}
