package com.dddrivebye.geolocation.application.query;

public record GetDriversWithinRadiusQuery(
        double latitude,
        double longitude,
        double radiusKm
) {
}
