package com.dddrivebye.matching.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class GroupingId {

    private final UUID value;

    private GroupingId(UUID value) {
        this.value = value;
    }

    public static GroupingId generate() {
        return new GroupingId(UUID.randomUUID());
    }

    public static GroupingId of(UUID value) {
        Objects.requireNonNull(value);
        return new GroupingId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupingId that)) return false;
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
