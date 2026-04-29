package com.dddrivebye.usermanagement.domain.event;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.usermanagement.domain.valueobject.AvailabilityStatus;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public final class DriverAvailabilityChangedEvent extends BaseDomainEvent {

    private final UserId userId;
    private final AvailabilityStatus previousStatus;
    private final AvailabilityStatus newStatus;

    public DriverAvailabilityChangedEvent(UserId userId, AvailabilityStatus previousStatus, AvailabilityStatus newStatus) {
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public UserId userId() {
        return userId;
    }

    public AvailabilityStatus previousStatus() {
        return previousStatus;
    }

    public AvailabilityStatus newStatus() {
        return newStatus;
    }
}
