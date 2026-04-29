package com.dddrivebye.matching.domain.event;

import com.dddrivebye.matching.domain.valueobject.GroupingId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

import java.util.List;
import java.util.UUID;

public final class GroupingCreatedEvent extends BaseDomainEvent {

    private final GroupingId groupingId;
    private final UUID driverId;
    private final List<UUID> rideRequestIds;

    public GroupingCreatedEvent(GroupingId groupingId, UUID driverId, List<UUID> rideRequestIds) {
        this.groupingId = groupingId;
        this.driverId = driverId;
        this.rideRequestIds = List.copyOf(rideRequestIds);
    }

    public GroupingId groupingId() {
        return groupingId;
    }

    public UUID driverId() {
        return driverId;
    }

    public List<UUID> rideRequestIds() {
        return rideRequestIds;
    }
}
