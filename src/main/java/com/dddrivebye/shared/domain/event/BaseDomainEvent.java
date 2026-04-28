package com.dddrivebye.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseDomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    public UUID eventId() {
        return eventId;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}
