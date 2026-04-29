package com.dddrivebye.territorialconfiguration.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class TerritoryId {

    private final UUID value;

    private TerritoryId(UUID value) {
        this.value = value;
    }

    public static TerritoryId generate() {
        return new TerritoryId(UUID.randomUUID());
    }

    public static TerritoryId of(UUID value) {
        Objects.requireNonNull(value);
        return new TerritoryId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TerritoryId that)) return false;
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
