package com.dddrivebye.matching.domain.entity;

import com.dddrivebye.matching.domain.event.GroupingCreatedEvent;
import com.dddrivebye.matching.domain.event.GroupingDissolvedEvent;
import com.dddrivebye.matching.domain.exception.InvalidGroupingOperationException;
import com.dddrivebye.matching.domain.valueobject.GroupingId;
import com.dddrivebye.matching.domain.valueobject.GroupingStatus;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Grouping {

    private final GroupingId id;
    private final UUID driverId;
    private final Set<UUID> rideRequestIds;
    private GroupingStatus status;

    private final transient List<BaseDomainEvent> domainEvents = new ArrayList<>();

    private Grouping(GroupingId id, UUID driverId, Set<UUID> rideRequestIds, GroupingStatus status) {
        this.id = id;
        this.driverId = driverId;
        this.rideRequestIds = rideRequestIds;
        this.status = status;
    }

    public static Grouping create(UUID driverId, List<UUID> rideRequestIds) {
        Objects.requireNonNull(driverId, "driverId");
        Objects.requireNonNull(rideRequestIds, "rideRequestIds");
        if (rideRequestIds.size() < 2) {
            throw new InvalidGroupingOperationException(
                    "A grouping requires at least 2 ride requests, got " + rideRequestIds.size());
        }
        Set<UUID> requests = new LinkedHashSet<>(rideRequestIds);
        if (requests.size() != rideRequestIds.size()) {
            throw new InvalidGroupingOperationException("Duplicate ride request in grouping");
        }
        Grouping grouping = new Grouping(GroupingId.generate(), driverId, requests, GroupingStatus.PROPOSED);
        grouping.domainEvents.add(new GroupingCreatedEvent(grouping.id, driverId, new ArrayList<>(requests)));
        return grouping;
    }

    public static Grouping reconstitute(GroupingId id, UUID driverId, Set<UUID> rideRequestIds, GroupingStatus status) {
        return new Grouping(id, driverId, new LinkedHashSet<>(rideRequestIds), status);
    }

    public GroupingId id() {
        return id;
    }

    public UUID driverId() {
        return driverId;
    }

    public Set<UUID> rideRequestIds() {
        return Collections.unmodifiableSet(rideRequestIds);
    }

    public GroupingStatus status() {
        return status;
    }

    public void confirm() {
        if (status != GroupingStatus.PROPOSED) {
            throw new InvalidGroupingOperationException(
                    "Cannot confirm grouping in status " + status);
        }
        status = GroupingStatus.ACTIVE;
    }

    public void dissolveBecauseOfCancellation(UUID cancellingPassengerRideRequestId) {
        if (status == GroupingStatus.DISSOLVED) {
            throw new InvalidGroupingOperationException("Grouping already dissolved");
        }
        if (!rideRequestIds.contains(cancellingPassengerRideRequestId)) {
            throw new InvalidGroupingOperationException(
                    "Ride request not part of this grouping: " + cancellingPassengerRideRequestId);
        }
        status = GroupingStatus.DISSOLVED;
        domainEvents.add(new GroupingDissolvedEvent(id, cancellingPassengerRideRequestId));
    }

    public List<BaseDomainEvent> pullDomainEvents() {
        List<BaseDomainEvent> snapshot = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(snapshot);
    }
}
