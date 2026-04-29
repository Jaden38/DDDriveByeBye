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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Match {

    public static final int DEFAULT_MAX_ATTEMPTS = 5;

    private final MatchId id;
    private final UUID rideId;
    private final RideKind kind;
    private final int maxAttempts;
    private final Set<UUID> excludedDriverIds;

    private MatchStatus status;
    private ProposalWindow proposal;
    private UUID acceptedDriverId;
    private int attemptCount;
    private String failureReason;

    private final transient List<BaseDomainEvent> domainEvents = new ArrayList<>();

    private Match(MatchId id,
                  UUID rideId,
                  RideKind kind,
                  int maxAttempts,
                  Set<UUID> excludedDriverIds,
                  MatchStatus status,
                  ProposalWindow proposal,
                  UUID acceptedDriverId,
                  int attemptCount,
                  String failureReason) {
        this.id = id;
        this.rideId = rideId;
        this.kind = kind;
        this.maxAttempts = maxAttempts;
        this.excludedDriverIds = excludedDriverIds;
        this.status = status;
        this.proposal = proposal;
        this.acceptedDriverId = acceptedDriverId;
        this.attemptCount = attemptCount;
        this.failureReason = failureReason;
    }

    public static Match create(UUID rideId, RideKind kind) {
        return create(rideId, kind, DEFAULT_MAX_ATTEMPTS);
    }

    public static Match create(UUID rideId, RideKind kind, int maxAttempts) {
        Objects.requireNonNull(rideId, "rideId");
        Objects.requireNonNull(kind, "kind");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        return new Match(
                MatchId.generate(),
                rideId,
                kind,
                maxAttempts,
                new HashSet<>(),
                MatchStatus.SEARCHING,
                null,
                null,
                0,
                null);
    }

    public static Match reconstitute(MatchId id,
                                     UUID rideId,
                                     RideKind kind,
                                     int maxAttempts,
                                     Set<UUID> excludedDriverIds,
                                     MatchStatus status,
                                     ProposalWindow proposal,
                                     UUID acceptedDriverId,
                                     int attemptCount,
                                     String failureReason) {
        return new Match(
                id,
                rideId,
                kind,
                maxAttempts,
                excludedDriverIds == null ? new HashSet<>() : new HashSet<>(excludedDriverIds),
                status,
                proposal,
                acceptedDriverId,
                attemptCount,
                failureReason);
    }

    public MatchId id() {
        return id;
    }

    public UUID rideId() {
        return rideId;
    }

    public RideKind kind() {
        return kind;
    }

    public MatchStatus status() {
        return status;
    }

    public Optional<ProposalWindow> proposal() {
        return Optional.ofNullable(proposal);
    }

    public Optional<UUID> acceptedDriverId() {
        return Optional.ofNullable(acceptedDriverId);
    }

    public int attemptCount() {
        return attemptCount;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Set<UUID> excludedDriverIds() {
        return Collections.unmodifiableSet(excludedDriverIds);
    }

    public Optional<String> failureReason() {
        return Optional.ofNullable(failureReason);
    }

    public void proposeTo(UUID driverId, Instant now, Duration window) {
        if (status != MatchStatus.SEARCHING) {
            throw new InvalidMatchOperationException(
                    "Cannot propose to a driver while match is in status " + status);
        }
        if (excludedDriverIds.contains(driverId)) {
            throw new InvalidMatchOperationException("Driver already excluded: " + driverId);
        }
        attemptCount++;
        proposal = ProposalWindow.opening(driverId, now, window);
        status = MatchStatus.PROPOSED;
        domainEvents.add(new MatchProposalSentEvent(id, rideId, driverId, proposal.expiresAt()));
    }

    public void accept() {
        if (status != MatchStatus.PROPOSED || proposal == null) {
            throw new InvalidMatchOperationException(
                    "No active proposal to accept (status=" + status + ")");
        }
        acceptedDriverId = proposal.driverId();
        status = MatchStatus.ACCEPTED;
        domainEvents.add(new MatchFoundEvent(id, rideId, acceptedDriverId));
        proposal = null;
    }

    public void declineCurrentProposal() {
        if (status != MatchStatus.PROPOSED || proposal == null) {
            throw new InvalidMatchOperationException(
                    "No active proposal to decline (status=" + status + ")");
        }
        UUID declined = proposal.driverId();
        excludedDriverIds.add(declined);
        proposal = null;
        if (attemptCount >= maxAttempts) {
            markUnmatched("Maximum rematch attempts reached");
        } else {
            status = MatchStatus.SEARCHING;
        }
    }

    public void expireCurrentProposal(Instant now) {
        if (status != MatchStatus.PROPOSED || proposal == null) {
            throw new InvalidMatchOperationException(
                    "No active proposal to expire (status=" + status + ")");
        }
        if (!proposal.hasExpiredAt(now)) {
            throw new InvalidMatchOperationException("Proposal has not yet expired");
        }
        declineCurrentProposal();
    }

    public void markUnmatched(String reason) {
        if (status.isTerminal()) {
            throw new InvalidMatchOperationException(
                    "Cannot mark unmatched: match already in terminal state " + status);
        }
        status = MatchStatus.UNMATCHED;
        failureReason = reason;
        proposal = null;
        domainEvents.add(new MatchFailedEvent(id, rideId, reason));
    }

    public void cancel() {
        if (status.isTerminal()) {
            throw new InvalidMatchOperationException(
                    "Cannot cancel: match already in terminal state " + status);
        }
        status = MatchStatus.CANCELLED;
        proposal = null;
    }

    public boolean canRetry() {
        return !status.isTerminal() && attemptCount < maxAttempts;
    }

    public List<BaseDomainEvent> pullDomainEvents() {
        List<BaseDomainEvent> snapshot = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(snapshot);
    }
}
