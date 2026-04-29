package com.dddrivebye.geolocation.domain.valueobject;

import java.time.Duration;
import java.util.Objects;

/**
 * Estimated Time of Arrival — duration before reaching the target.
 */
public final class Eta {

    private final Duration duration;

    private Eta(Duration duration) {
        this.duration = duration;
    }

    public static Eta ofMinutes(long minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("ETA cannot be negative: " + minutes);
        }
        return new Eta(Duration.ofMinutes(minutes));
    }

    public static Eta of(Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("ETA must be non-negative");
        }
        return new Eta(duration);
    }

    public Duration duration() {
        return duration;
    }

    public long toMinutes() {
        return duration.toMinutes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Eta that)) return false;
        return duration.equals(that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration);
    }

    @Override
    public String toString() {
        return duration.toMinutes() + " min";
    }
}
