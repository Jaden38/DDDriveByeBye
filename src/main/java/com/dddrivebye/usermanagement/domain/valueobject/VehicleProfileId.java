package com.dddrivebye.usermanagement.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class VehicleProfileId {

    private final UUID value;

    private VehicleProfileId(UUID value) {
        this.value = value;
    }

    public static VehicleProfileId generate() {
        return new VehicleProfileId(UUID.randomUUID());
    }

    public static VehicleProfileId of(UUID value) {
        Objects.requireNonNull(value);
        return new VehicleProfileId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleProfileId that)) return false;
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
