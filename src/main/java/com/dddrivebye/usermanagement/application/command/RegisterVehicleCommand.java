package com.dddrivebye.usermanagement.application.command;

import java.util.Set;
import java.util.UUID;

public record RegisterVehicleCommand(
        UUID userId,
        String make,
        String model,
        int year,
        String licensePlate,
        int seats,
        String fuelType,
        Set<String> options
) {
}
