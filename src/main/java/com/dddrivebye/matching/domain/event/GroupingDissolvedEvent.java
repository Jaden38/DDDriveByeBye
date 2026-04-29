package com.dddrivebye.matching.domain.event;

import com.dddrivebye.matching.domain.valueobject.GroupingId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

public final class GroupingDissolvedEvent extends BaseDomainEvent {

    private final GroupingId groupingId;
    private final UUID cancellingPassengerId;

    public GroupingDissolvedEvent(GroupingId groupingId, UUID cancellingPassengerId) {
        this.groupingId = groupingId;
        this.cancellingPassengerId = cancellingPassengerId;
    }

    public GroupingId groupingId() {
        return groupingId;
    }

    public UUID cancellingPassengerId() {
        return cancellingPassengerId;
    }
}
