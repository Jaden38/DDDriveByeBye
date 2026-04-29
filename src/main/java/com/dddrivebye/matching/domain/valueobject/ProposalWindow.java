package com.dddrivebye.matching.domain.valueobject;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ProposalWindow {

    public static final Duration DEFAULT_RESPONSE_WINDOW = Duration.ofSeconds(30);

    private final UUID driverId;
    private final Instant expiresAt;

    private ProposalWindow(UUID driverId, Instant expiresAt) {
        this.driverId = driverId;
        this.expiresAt = expiresAt;
    }

    public static ProposalWindow opening(UUID driverId, Instant now, Duration window) {
        Objects.requireNonNull(driverId, "driverId");
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(window, "window");
        if (window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("window must be positive");
        }
        return new ProposalWindow(driverId, now.plus(window));
    }

    public static ProposalWindow rehydrate(UUID driverId, Instant expiresAt) {
        Objects.requireNonNull(driverId);
        Objects.requireNonNull(expiresAt);
        return new ProposalWindow(driverId, expiresAt);
    }

    public UUID driverId() {
        return driverId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean hasExpiredAt(Instant moment) {
        return !moment.isBefore(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProposalWindow that)) return false;
        return driverId.equals(that.driverId) && expiresAt.equals(that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId, expiresAt);
    }
}
