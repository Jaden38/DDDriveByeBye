package com.dddrivebye.matching.application.handler;

import com.dddrivebye.matching.application.command.DissolveGroupingCommand;
import com.dddrivebye.matching.application.command.EvaluateGroupingCommand;
import com.dddrivebye.matching.domain.entity.Grouping;
import com.dddrivebye.matching.domain.exception.InvalidGroupingOperationException;
import com.dddrivebye.matching.domain.repository.GroupingRepository;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupingCommandHandler {

    private final GroupingRepository groupings;
    private final DomainEventPublisher events;

    public GroupingCommandHandler(GroupingRepository groupings, DomainEventPublisher events) {
        this.groupings = groupings;
        this.events = events;
    }

    @Transactional
    public Optional<UUID> handle(EvaluateGroupingCommand command) {
        if (!command.territoryCarpoolingEnabled()) {
            return Optional.empty();
        }
        List<EvaluateGroupingCommand.RideRequestCandidate> eligible = command.candidates().stream()
                .filter(EvaluateGroupingCommand.RideRequestCandidate::carpoolingOptIn)
                .toList();
        if (eligible.size() < 2) {
            return Optional.empty();
        }
        if (command.driverSeatsAvailable() < eligible.size()) {
            return Optional.empty();
        }
        if (!sharedDestination(eligible)) {
            return Optional.empty();
        }
        if (!pickupsClustered(eligible, command.pickupProximityKm())) {
            return Optional.empty();
        }
        Grouping grouping = Grouping.create(
                command.driverId(),
                eligible.stream().map(EvaluateGroupingCommand.RideRequestCandidate::rideRequestId).toList());
        persist(grouping);
        return Optional.of(grouping.id().value());
    }

    @Transactional
    public void handle(DissolveGroupingCommand command) {
        Grouping grouping = groupings.findActiveByRideRequestId(command.rideRequestId())
                .orElseThrow(() -> new InvalidGroupingOperationException(
                        "No active grouping containing ride request " + command.rideRequestId()));
        grouping.dissolveBecauseOfCancellation(command.rideRequestId());
        persist(grouping);
    }

    private boolean sharedDestination(List<EvaluateGroupingCommand.RideRequestCandidate> candidates) {
        var first = candidates.get(0).destination();
        return candidates.stream().allMatch(c -> c.destination().equals(first));
    }

    private boolean pickupsClustered(List<EvaluateGroupingCommand.RideRequestCandidate> candidates,
                                     double maxDistanceKm) {
        var pivot = candidates.get(0).pickup();
        return candidates.stream()
                .skip(1)
                .allMatch(c -> pivot.distanceKmTo(c.pickup()) <= maxDistanceKm);
    }

    private void persist(Grouping grouping) {
        groupings.save(grouping);
        for (BaseDomainEvent event : grouping.pullDomainEvents()) {
            events.publish(event);
        }
    }
}
