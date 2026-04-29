package com.dddrivebye.ridemanagement.domain.event;

import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

public final class RideAcceptedEvent extends BaseDomainEvent {
    private final RideId rideId;

    public RideAcceptedEvent(RideId rideId) {
        this.rideId = rideId;
    }

    public RideId rideId() {
        return rideId;
    }
}
