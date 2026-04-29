package com.dddrivebye.matching.domain.entity;

import com.dddrivebye.matching.domain.event.GroupingCreatedEvent;
import com.dddrivebye.matching.domain.event.GroupingDissolvedEvent;
import com.dddrivebye.matching.domain.exception.InvalidGroupingOperationException;
import com.dddrivebye.matching.domain.valueobject.GroupingId;
import com.dddrivebye.matching.domain.valueobject.GroupingStatus;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupingTest {

    @Test
    void shouldCreateGroupingAndRegisterEvent() {
        UUID driver = UUID.randomUUID();
        UUID alice = UUID.randomUUID();
        UUID charlie = UUID.randomUUID();

        Grouping grouping = Grouping.create(driver, List.of(alice, charlie));

        assertThat(grouping.id()).isNotNull();
        assertThat(grouping.driverId()).isEqualTo(driver);
        assertThat(grouping.status()).isEqualTo(GroupingStatus.PROPOSED);
        assertThat(grouping.rideRequestIds()).containsExactly(alice, charlie);

        List<BaseDomainEvent> events = grouping.pullDomainEvents();
        assertThat(events).hasSize(1);
        GroupingCreatedEvent created = (GroupingCreatedEvent) events.get(0);
        assertThat(created.driverId()).isEqualTo(driver);
        assertThat(created.rideRequestIds()).containsExactly(alice, charlie);
    }

    @Test
    void shouldRejectGroupingOfFewerThanTwo() {
        assertThatThrownBy(() -> Grouping.create(UUID.randomUUID(), List.of()))
                .isInstanceOf(InvalidGroupingOperationException.class);
        assertThatThrownBy(() -> Grouping.create(UUID.randomUUID(), List.of(UUID.randomUUID())))
                .isInstanceOf(InvalidGroupingOperationException.class);
    }

    @Test
    void shouldRejectDuplicateRideRequests() {
        UUID alice = UUID.randomUUID();

        assertThatThrownBy(() -> Grouping.create(UUID.randomUUID(), List.of(alice, alice)))
                .isInstanceOf(InvalidGroupingOperationException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void shouldConfirmFromProposed() {
        Grouping grouping = Grouping.create(UUID.randomUUID(),
                List.of(UUID.randomUUID(), UUID.randomUUID()));

        grouping.confirm();

        assertThat(grouping.status()).isEqualTo(GroupingStatus.ACTIVE);
    }

    @Test
    void shouldRejectConfirmFromNonProposedState() {
        UUID alice = UUID.randomUUID();
        Grouping grouping = Grouping.create(UUID.randomUUID(), List.of(alice, UUID.randomUUID()));
        grouping.dissolveBecauseOfCancellation(alice);

        assertThatThrownBy(grouping::confirm)
                .isInstanceOf(InvalidGroupingOperationException.class);
    }

    @Test
    void shouldDissolveOnCancellationAndEmitEvent() {
        UUID alice = UUID.randomUUID();
        UUID charlie = UUID.randomUUID();
        Grouping grouping = Grouping.create(UUID.randomUUID(), List.of(alice, charlie));
        grouping.pullDomainEvents();

        grouping.dissolveBecauseOfCancellation(alice);

        assertThat(grouping.status()).isEqualTo(GroupingStatus.DISSOLVED);
        List<BaseDomainEvent> events = grouping.pullDomainEvents();
        assertThat(events).hasSize(1);
        GroupingDissolvedEvent dissolved = (GroupingDissolvedEvent) events.get(0);
        assertThat(dissolved.cancellingPassengerId()).isEqualTo(alice);
    }

    @Test
    void shouldRejectDissolveForUnknownRideRequest() {
        Grouping grouping = Grouping.create(UUID.randomUUID(),
                List.of(UUID.randomUUID(), UUID.randomUUID()));

        assertThatThrownBy(() -> grouping.dissolveBecauseOfCancellation(UUID.randomUUID()))
                .isInstanceOf(InvalidGroupingOperationException.class)
                .hasMessageContaining("not part of this grouping");
    }

    @Test
    void shouldRejectDissolveWhenAlreadyDissolved() {
        UUID alice = UUID.randomUUID();
        Grouping grouping = Grouping.create(UUID.randomUUID(), List.of(alice, UUID.randomUUID()));
        grouping.dissolveBecauseOfCancellation(alice);

        assertThatThrownBy(() -> grouping.dissolveBecauseOfCancellation(alice))
                .isInstanceOf(InvalidGroupingOperationException.class)
                .hasMessageContaining("already dissolved");
    }

    @Test
    void shouldReconstituteFromPersistedState() {
        GroupingId id = GroupingId.generate();
        UUID driver = UUID.randomUUID();
        LinkedHashSet<UUID> requests = new LinkedHashSet<>(
                List.of(UUID.randomUUID(), UUID.randomUUID()));

        Grouping grouping = Grouping.reconstitute(id, driver, requests, GroupingStatus.ACTIVE);

        assertThat(grouping.id()).isEqualTo(id);
        assertThat(grouping.driverId()).isEqualTo(driver);
        assertThat(grouping.status()).isEqualTo(GroupingStatus.ACTIVE);
        assertThat(grouping.rideRequestIds()).containsExactlyElementsOf(requests);
        assertThat(grouping.pullDomainEvents()).isEmpty();
    }
}
