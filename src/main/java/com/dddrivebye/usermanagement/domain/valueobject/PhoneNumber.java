package com.dddrivebye.usermanagement.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class PhoneNumber {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 .()-]{6,}$");

    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String value) {
        Objects.requireNonNull(value, "phone must not be null");
        String trimmed = value.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
        return new PhoneNumber(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber that)) return false;
        return value.equals(that.value);
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
