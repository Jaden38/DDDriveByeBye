package com.dddrivebye.matching.domain.event;

import com.dddrivebye.matching.domain.valueobject.MatchId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

import java.time.Instant;
import java.util.UUID;

public final class MatchProposalSentEvent extends BaseDomainEvent {

    private final MatchId matchId;
    private final UUID rideId;
    private final UUID driverId;
    private final Instant expiresAt;

    public MatchProposalSentEvent(MatchId matchId, UUID rideId, UUID driverId, Instant expiresAt) {
        this.matchId = matchId;
        this.rideId = rideId;
        this.driverId = driverId;
        this.expiresAt = expiresAt;
    }

    public MatchId matchId() {
        return matchId;
    }

    public UUID rideId() {
        return rideId;
    }

    public UUID driverId() {
        return driverId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
