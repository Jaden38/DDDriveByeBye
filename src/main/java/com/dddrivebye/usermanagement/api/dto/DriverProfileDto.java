package com.dddrivebye.usermanagement.api.dto;

import java.util.List;
import java.util.UUID;

public record DriverProfileDto(
        UUID userId,
        String accountType,
        String status,
        String availabilityStatus,
        String workingZoneLabel,
        String activityZoneLabel,
        List<VehicleDto> vehicles
) {

    public record VehicleDto(
            UUID id,
            String make,
            String model,
            int year,
            String licensePlate,
            int seats,
            String fuelType,
            List<String> options
    ) {
    }
}
