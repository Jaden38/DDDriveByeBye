package com.dddrivebye.usermanagement.application.command;

import java.util.UUID;

public record ActivateAvailabilityCommand(UUID userId, boolean hasActivePassengerRide) {
}
