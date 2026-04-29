package com.dddrivebye.geolocation.api.dto;

import java.util.UUID;

public record NearbyDriverDto(
        UUID driverId,
        double latitude,
        double longitude,
        double distanceKm
) {
}
