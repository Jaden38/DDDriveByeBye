package com.dddrivebye.usermanagement.domain.event;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public final class AccountRestrictedEvent extends BaseDomainEvent {

    private final UserId userId;

    public AccountRestrictedEvent(UserId userId) {
        this.userId = userId;
    }

    public UserId userId() {
        return userId;
    }
}
