package com.dddrivebye.matching.domain.event;

import com.dddrivebye.matching.domain.valueobject.MatchId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

public final class MatchFoundEvent extends BaseDomainEvent {

    private final MatchId matchId;
    private final UUID rideId;
    private final UUID driverId;

    public MatchFoundEvent(MatchId matchId, UUID rideId, UUID driverId) {
        this.matchId = matchId;
        this.rideId = rideId;
        this.driverId = driverId;
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
}
