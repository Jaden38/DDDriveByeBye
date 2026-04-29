package com.dddrivebye.ridemanagement.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RideRequestId(UUID value) {
    public RideRequestId {
        Objects.requireNonNull(value, "RideRequestId value cannot be null");
    }

    public static RideRequestId generate() {
        return new RideRequestId(UUID.randomUUID());
    }

    public static RideRequestId of(UUID value) {
        return new RideRequestId(value);
    }
}
