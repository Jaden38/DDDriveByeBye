package com.dddrivebye.usermanagement.application.query;

public record GetAvailableDriversNearQuery(
        double latitude,
        double longitude,
        double radiusKm,
        String requiredAccountType
) {
}
