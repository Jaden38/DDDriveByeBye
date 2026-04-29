package com.dddrivebye.matching.api.dto;

import java.util.List;
import java.util.UUID;

public record MatchDto(
        UUID matchId,
        UUID rideId,
        String kind,
        String status,
        UUID proposedDriverId,
        String proposalExpiresAt,
        UUID acceptedDriverId,
        int attemptCount,
        int maxAttempts,
        String failureReason,
        List<UUID> excludedDriverIds
) {
}
