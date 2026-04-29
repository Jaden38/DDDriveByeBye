package com.dddrivebye.matching.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class MatchId {

    private final UUID value;

    private MatchId(UUID value) {
        this.value = value;
    }

    public static MatchId generate() {
        return new MatchId(UUID.randomUUID());
    }

    public static MatchId of(UUID value) {
        Objects.requireNonNull(value);
        return new MatchId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
