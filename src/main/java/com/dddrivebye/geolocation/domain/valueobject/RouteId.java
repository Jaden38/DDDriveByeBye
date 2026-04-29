package com.dddrivebye.geolocation.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class RouteId {

    private final UUID value;

    private RouteId(UUID value) {
        this.value = value;
    }

    public static RouteId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("RouteId cannot be null");
        }
        return new RouteId(value);
    }

    public static RouteId generate() {
        return new RouteId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteId that)) return false;
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
