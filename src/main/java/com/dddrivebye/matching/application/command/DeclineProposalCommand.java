package com.dddrivebye.matching.application.command;

import java.util.UUID;

public record DeclineProposalCommand(UUID rideId, UUID driverId) {
}
