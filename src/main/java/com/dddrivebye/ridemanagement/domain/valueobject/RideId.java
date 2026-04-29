package com.dddrivebye.ridemanagement.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RideId(UUID value) {
    public RideId {
        Objects.requireNonNull(value, "RideId value cannot be null");
    }

    public static RideId generate() {
        return new RideId(UUID.randomUUID());
    }

    public static RideId of(UUID value) {
        return new RideId(value);
    }

    public static RideId of(String value) {
        return new RideId(UUID.fromString(value));
    }
}
