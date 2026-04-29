package com.dddrivebye.matching.domain.event;

import com.dddrivebye.matching.domain.valueobject.MatchId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

public final class MatchFailedEvent extends BaseDomainEvent {

    private final MatchId matchId;
    private final UUID rideId;
    private final String reason;

    public MatchFailedEvent(MatchId matchId, UUID rideId, String reason) {
        this.matchId = matchId;
        this.rideId = rideId;
        this.reason = reason;
    }

    public MatchId matchId() {
        return matchId;
    }

    public UUID rideId() {
        return rideId;
    }

    public String reason() {
        return reason;
    }
}
