package com.dddrivebye.usermanagement.domain.event;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public final class DriverProfileRejectedEvent extends BaseDomainEvent {

    private final UserId userId;
    private final String reason;

    public DriverProfileRejectedEvent(UserId userId, String reason) {
        this.userId = userId;
        this.reason = reason;
    }

    public UserId userId() {
        return userId;
    }

    public String reason() {
        return reason;
    }
}
