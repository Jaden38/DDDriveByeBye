package com.dddrivebye.usermanagement.domain.event;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public final class DriverProfileValidatedEvent extends BaseDomainEvent {

    private final UserId userId;

    public DriverProfileValidatedEvent(UserId userId) {
        this.userId = userId;
    }

    public UserId userId() {
        return userId;
    }
}
