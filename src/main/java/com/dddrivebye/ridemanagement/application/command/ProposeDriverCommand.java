package com.dddrivebye.ridemanagement.application.command;

import java.util.UUID;

public record ProposeDriverCommand(UUID rideId, UUID driverId) {
}
