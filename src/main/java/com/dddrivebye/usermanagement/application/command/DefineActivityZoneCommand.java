package com.dddrivebye.usermanagement.application.command;

import java.util.UUID;

public record DefineActivityZoneCommand(
        UUID userId,
        String label,
        double latitude,
        double longitude,
        double radiusKm
) {
}
