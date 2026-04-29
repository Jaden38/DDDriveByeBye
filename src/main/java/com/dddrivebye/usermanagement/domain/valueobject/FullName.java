package com.dddrivebye.usermanagement.domain.valueobject;

import java.util.Objects;

public final class FullName {

    private final String value;

    private FullName(String value) {
        this.value = value;
    }

    public static FullName of(String value) {
        Objects.requireNonNull(value, "full name must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Full name must not be empty");
        }
        return new FullName(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullName fullName)) return false;
        return value.equals(fullName.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
