package com.dddrivebye.ridemanagement.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RideOfferId(UUID value) {
    public RideOfferId {
        Objects.requireNonNull(value, "RideOfferId value cannot be null");
    }

    public static RideOfferId generate() {
        return new RideOfferId(UUID.randomUUID());
    }

    public static RideOfferId of(UUID value) {
        return new RideOfferId(value);
    }
}
