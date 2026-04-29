package com.dddrivebye.matching.domain.entity;

import com.dddrivebye.matching.domain.event.MatchFailedEvent;
import com.dddrivebye.matching.domain.event.MatchFoundEvent;
import com.dddrivebye.matching.domain.event.MatchProposalSentEvent;
import com.dddrivebye.matching.domain.exception.InvalidMatchOperationException;
import com.dddrivebye.matching.domain.valueobject.MatchId;
import com.dddrivebye.matching.domain.valueobject.MatchStatus;
import com.dddrivebye.matching.domain.valueobject.ProposalWindow;
import com.dddrivebye.matching.domain.valueobject.RideKind;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchTest {

    private static final Duration THIRTY_SECONDS = Duration.ofSeconds(30);

    @Test
    void shouldCreateMatchInSearchingState() {
        UUID rideId = UUID.randomUUID();

        Match match = Match.create(rideId, RideKind.IMMEDIATE);

        assertThat(match.id()).isNotNull();
        assertThat(match.rideId()).isEqualTo(rideId);
        assertThat(match.kind()).isEqualTo(RideKind.IMMEDIATE);
        assertThat(match.status()).isEqualTo(MatchStatus.SEARCHING);
        assertThat(match.attemptCount()).isZero();
        assertThat(match.maxAttempts()).isEqualTo(Match.DEFAULT_MAX_ATTEMPTS);
        assertThat(match.proposal()).isEmpty();
        assertThat(match.acceptedDriverId()).isEmpty();
        assertThat(match.excludedDriverIds()).isEmpty();
        assertThat(match.failureReason()).isEmpty();
        assertThat(match.canRetry()).isTrue();
    }

    @Test
    void shouldRejectNullArgumentsOnCreate() {
        assertThatThrownBy(() -> Match.create(null, RideKind.IMMEDIATE))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Match.create(UUID.randomUUID(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectMaxAttemptsBelowOne() {
        assertThatThrownBy(() -> Match.create(UUID.randomUUID(), RideKind.IMMEDIATE, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Match.create(UUID.randomUUID(), RideKind.IMMEDIATE, -3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldMoveToProposedWhenSendingProposal() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");

        match.proposeTo(driver, now, THIRTY_SECONDS);

        assertThat(match.status()).isEqualTo(MatchStatus.PROPOSED);
        assertThat(match.attemptCount()).isEqualTo(1);
        assertThat(match.proposal()).isPresent();
        assertThat(match.proposal().get().driverId()).isEqualTo(driver);
        assertThat(match.proposal().get().expiresAt()).isEqualTo(now.plus(THIRTY_SECONDS));

        List<BaseDomainEvent> events = match.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(MatchProposalSentEvent.class);
        MatchProposalSentEvent sent = (MatchProposalSentEvent) events.get(0);
        assertThat(sent.matchId()).isEqualTo(match.id());
        assertThat(sent.rideId()).isEqualTo(match.rideId());
        assertThat(sent.driverId()).isEqualTo(driver);
        assertThat(sent.expiresAt()).isEqualTo(now.plus(THIRTY_SECONDS));
    }

    @Test
    void shouldRejectProposalWhenNotSearching() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(driver, now, THIRTY_SECONDS);

        assertThatThrownBy(() -> match.proposeTo(UUID.randomUUID(), now, THIRTY_SECONDS))
                .isInstanceOf(InvalidMatchOperationException.class)
                .hasMessageContaining("PROPOSED");
    }

    @Test
    void shouldRejectProposalToAlreadyExcludedDriver() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(driver, now, THIRTY_SECONDS);
        match.declineCurrentProposal();

        assertThatThrownBy(() -> match.proposeTo(driver, now, THIRTY_SECONDS))
                .isInstanceOf(InvalidMatchOperationException.class)
                .hasMessageContaining("excluded");
    }

    @Test
    void shouldAcceptProposalAndEmitMatchFoundEvent() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(driver, now, THIRTY_SECONDS);
        match.pullDomainEvents();

        match.accept();

        assertThat(match.status()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(match.acceptedDriverId()).contains(driver);
        assertThat(match.proposal()).isEmpty();
        List<BaseDomainEvent> events = match.pullDomainEvents();
        assertThat(events).hasSize(1);
        MatchFoundEvent found = (MatchFoundEvent) events.get(0);
        assertThat(found.driverId()).isEqualTo(driver);
        assertThat(found.rideId()).isEqualTo(match.rideId());
    }

    @Test
    void shouldRejectAcceptWithoutActiveProposal() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);

        assertThatThrownBy(match::accept)
                .isInstanceOf(InvalidMatchOperationException.class);
    }

    @Test
    void shouldExcludeDriverAndReturnToSearchingOnDecline() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(driver, now, THIRTY_SECONDS);
        match.pullDomainEvents();

        match.declineCurrentProposal();

        assertThat(match.status()).isEqualTo(MatchStatus.SEARCHING);
        assertThat(match.excludedDriverIds()).containsExactly(driver);
        assertThat(match.proposal()).isEmpty();
        assertThat(match.pullDomainEvents()).isEmpty();
        assertThat(match.canRetry()).isTrue();
    }

    @Test
    void shouldMarkUnmatchedAfterReachingMaxAttempts() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE, 2);
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(UUID.randomUUID(), now, THIRTY_SECONDS);
        match.declineCurrentProposal();
        match.proposeTo(UUID.randomUUID(), now, THIRTY_SECONDS);
        match.pullDomainEvents();

        match.declineCurrentProposal();

        assertThat(match.status()).isEqualTo(MatchStatus.UNMATCHED);
        assertThat(match.failureReason()).contains("Maximum rematch attempts reached");
        assertThat(match.canRetry()).isFalse();
        List<BaseDomainEvent> events = match.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(MatchFailedEvent.class);
    }

    @Test
    void shouldRejectExpireBeforeWindowEnd() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(UUID.randomUUID(), now, THIRTY_SECONDS);

        assertThatThrownBy(() -> match.expireCurrentProposal(now.plusSeconds(15)))
                .isInstanceOf(InvalidMatchOperationException.class)
                .hasMessageContaining("not yet expired");
    }

    @Test
    void shouldExpireProposalAtOrAfterDeadline() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(driver, now, THIRTY_SECONDS);
        match.pullDomainEvents();

        match.expireCurrentProposal(now.plus(THIRTY_SECONDS));

        assertThat(match.status()).isEqualTo(MatchStatus.SEARCHING);
        assertThat(match.excludedDriverIds()).containsExactly(driver);
    }

    @Test
    void shouldMarkUnmatchedAndEmitFailedEvent() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);

        match.markUnmatched("nope");

        assertThat(match.status()).isEqualTo(MatchStatus.UNMATCHED);
        assertThat(match.failureReason()).contains("nope");
        List<BaseDomainEvent> events = match.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(MatchFailedEvent.class);
    }

    @Test
    void shouldRejectMarkUnmatchedFromTerminalState() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        match.markUnmatched("terminal");

        assertThatThrownBy(() -> match.markUnmatched("again"))
                .isInstanceOf(InvalidMatchOperationException.class);
    }

    @Test
    void shouldCancelFromNonTerminalState() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);

        match.cancel();

        assertThat(match.status()).isEqualTo(MatchStatus.CANCELLED);
        assertThat(match.proposal()).isEmpty();
    }

    @Test
    void shouldRejectCancelFromTerminalState() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        match.cancel();

        assertThatThrownBy(match::cancel).isInstanceOf(InvalidMatchOperationException.class);
    }

    @Test
    void shouldClearEventsOnPull() {
        Match match = Match.create(UUID.randomUUID(), RideKind.IMMEDIATE);
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        match.proposeTo(UUID.randomUUID(), now, THIRTY_SECONDS);

        match.pullDomainEvents();

        assertThat(match.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldReconstituteFromPersistedState() {
        UUID rideId = UUID.randomUUID();
        UUID excluded = UUID.randomUUID();
        UUID accepted = UUID.randomUUID();
        MatchId id = MatchId.generate();
        ProposalWindow proposal = ProposalWindow.rehydrate(
                UUID.randomUUID(), Instant.parse("2026-04-29T10:00:30Z"));

        Match match = Match.reconstitute(
                id, rideId, RideKind.SCHEDULED, 4,
                new HashSet<>(List.of(excluded)),
                MatchStatus.PROPOSED, proposal, accepted, 2, null);

        assertThat(match.id()).isEqualTo(id);
        assertThat(match.kind()).isEqualTo(RideKind.SCHEDULED);
        assertThat(match.maxAttempts()).isEqualTo(4);
        assertThat(match.attemptCount()).isEqualTo(2);
        assertThat(match.excludedDriverIds()).containsExactly(excluded);
        assertThat(match.proposal()).contains(proposal);
        assertThat(match.acceptedDriverId()).contains(accepted);
        assertThat(match.canRetry()).isTrue();
    }

    @Test
    void shouldHandleNullExcludedSetOnReconstitute() {
        Match match = Match.reconstitute(
                MatchId.generate(), UUID.randomUUID(), RideKind.IMMEDIATE, 5, null,
                MatchStatus.SEARCHING, null, null, 0, null);

        assertThat(match.excludedDriverIds()).isEmpty();
    }
}
