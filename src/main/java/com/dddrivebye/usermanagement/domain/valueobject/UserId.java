package com.dddrivebye.usermanagement.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        this.value = value;
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        Objects.requireNonNull(value, "UserId value must not be null");
        return new UserId(value);
    }

    public static UserId of(String value) {
        return of(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId userId)) return false;
        return value.equals(userId.value);
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
