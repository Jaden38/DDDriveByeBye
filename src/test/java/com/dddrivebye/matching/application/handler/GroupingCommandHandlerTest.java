package com.dddrivebye.matching.application.handler;

import com.dddrivebye.matching.application.command.DissolveGroupingCommand;
import com.dddrivebye.matching.application.command.EvaluateGroupingCommand;
import com.dddrivebye.matching.application.command.EvaluateGroupingCommand.RideRequestCandidate;
import com.dddrivebye.matching.domain.entity.Grouping;
import com.dddrivebye.matching.domain.event.GroupingCreatedEvent;
import com.dddrivebye.matching.domain.event.GroupingDissolvedEvent;
import com.dddrivebye.matching.domain.exception.InvalidGroupingOperationException;
import com.dddrivebye.matching.domain.repository.GroupingRepository;
import com.dddrivebye.matching.domain.valueobject.GroupingStatus;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupingCommandHandlerTest {

    private static final GeoCoordinates MONTPARNASSE = GeoCoordinates.of(48.84, 2.32);
    private static final GeoCoordinates CDG = GeoCoordinates.of(49.00, 2.55);

    @Mock private GroupingRepository groupings;
    @Mock private DomainEventPublisher events;

    private GroupingCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GroupingCommandHandler(groupings, events);
    }

    @Test
    void shouldNotCreateGroupingWhenTerritoryDisablesCarpooling() {
        EvaluateGroupingCommand command = new EvaluateGroupingCommand(
                UUID.randomUUID(), 3,
                List.of(candidate(MONTPARNASSE, true), candidate(MONTPARNASSE, true)),
                0.5, /*territoryCarpoolingEnabled*/ false);

        Optional<UUID> result = handler.handle(command);

        assertThat(result).isEmpty();
        verify(groupings, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotCreateGroupingWhenFewerThanTwoOptIns() {
        EvaluateGroupingCommand command = new EvaluateGroupingCommand(
                UUID.randomUUID(), 3,
                List.of(candidate(MONTPARNASSE, true), candidate(MONTPARNASSE, false)),
                0.5, true);

        Optional<UUID> result = handler.handle(command);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotCreateGroupingWhenDriverHasInsufficientSeats() {
        EvaluateGroupingCommand command = new EvaluateGroupingCommand(
                UUID.randomUUID(), 1,
                List.of(candidate(MONTPARNASSE, true), candidate(MONTPARNASSE, true)),
                0.5, true);

        Optional<UUID> result = handler.handle(command);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotCreateGroupingWhenDestinationsDiffer() {
        EvaluateGroupingCommand command = new EvaluateGroupingCommand(
                UUID.randomUUID(), 3,
                List.of(
                        candidate(MONTPARNASSE, MONTPARNASSE, true),
                        candidate(MONTPARNASSE, CDG, true)),
                0.5, true);

        Optional<UUID> result = handler.handle(command);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotCreateGroupingWhenPickupsTooFarApart() {
        GeoCoordinates near = GeoCoordinates.of(48.840, 2.320);
        GeoCoordinates far = GeoCoordinates.of(48.860, 2.350);  // ~3 km away
        EvaluateGroupingCommand command = new EvaluateGroupingCommand(
                UUID.randomUUID(), 3,
                List.of(
                        new RideRequestCandidate(UUID.randomUUID(), UUID.randomUUID(), near, MONTPARNASSE, true),
                        new RideRequestCandidate(UUID.randomUUID(), UUID.randomUUID(), far, MONTPARNASSE, true)),
                0.5, true);

        Optional<UUID> result = handler.handle(command);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCreateGroupingAndPublishEventWhenAllRulesSatisfied() {
        UUID driver = UUID.randomUUID();
        RideRequestCandidate alice = candidate(MONTPARNASSE, true);
        RideRequestCandidate charlie = candidate(MONTPARNASSE, true);
        EvaluateGroupingCommand command = new EvaluateGroupingCommand(
                driver, 3, List.of(alice, charlie), 0.5, true);

        Optional<UUID> result = handler.handle(command);

        assertThat(result).isPresent();
        ArgumentCaptor<Grouping> captor = ArgumentCaptor.forClass(Grouping.class);
        verify(groupings).save(captor.capture());
        Grouping saved = captor.getValue();
        assertThat(saved.driverId()).isEqualTo(driver);
        assertThat(saved.status()).isEqualTo(GroupingStatus.PROPOSED);
        assertThat(saved.rideRequestIds()).containsExactly(alice.rideRequestId(), charlie.rideRequestId());

        ArgumentCaptor<BaseDomainEvent> eventCaptor = ArgumentCaptor.forClass(BaseDomainEvent.class);
        verify(events).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(GroupingCreatedEvent.class);
    }

    @Test
    void shouldDissolveExistingGroupingOnPassengerCancellation() {
        UUID driver = UUID.randomUUID();
        UUID alice = UUID.randomUUID();
        UUID charlie = UUID.randomUUID();
        Grouping grouping = Grouping.create(driver, List.of(alice, charlie));
        grouping.pullDomainEvents();
        when(groupings.findActiveByRideRequestId(alice)).thenReturn(Optional.of(grouping));

        handler.handle(new DissolveGroupingCommand(alice));

        ArgumentCaptor<Grouping> captor = ArgumentCaptor.forClass(Grouping.class);
        verify(groupings).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(GroupingStatus.DISSOLVED);

        ArgumentCaptor<BaseDomainEvent> eventCaptor = ArgumentCaptor.forClass(BaseDomainEvent.class);
        verify(events).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(GroupingDissolvedEvent.class);
    }

    @Test
    void shouldFailDissolveWhenNoActiveGroupingFound() {
        UUID alice = UUID.randomUUID();
        when(groupings.findActiveByRideRequestId(alice)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DissolveGroupingCommand(alice)))
                .isInstanceOf(InvalidGroupingOperationException.class);
    }

    private static RideRequestCandidate candidate(GeoCoordinates pickup, boolean carpoolingOptIn) {
        return new RideRequestCandidate(UUID.randomUUID(), UUID.randomUUID(), pickup, MONTPARNASSE, carpoolingOptIn);
    }

    private static RideRequestCandidate candidate(GeoCoordinates pickup,
                                                  GeoCoordinates destination,
                                                  boolean carpoolingOptIn) {
        return new RideRequestCandidate(UUID.randomUUID(), UUID.randomUUID(), pickup, destination, carpoolingOptIn);
    }
}
