package com.dddrivebye.matching.application.command;

import java.util.UUID;

public record AcceptProposalCommand(UUID rideId, UUID driverId) {
}
