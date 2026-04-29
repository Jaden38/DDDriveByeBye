package com.dddrivebye.geolocation.application.command;

import java.util.UUID;

public record UpdateDriverPositionCommand(
        UUID driverId,
        double latitude,
        double longitude
) {
}
