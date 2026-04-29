package com.dddrivebye.geolocation.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class DriverId {

    private final UUID value;

    private DriverId(UUID value) {
        this.value = value;
    }

    public static DriverId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("DriverId cannot be null");
        }
        return new DriverId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriverId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
