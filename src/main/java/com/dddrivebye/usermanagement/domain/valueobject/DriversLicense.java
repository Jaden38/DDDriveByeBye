package com.dddrivebye.usermanagement.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

public final class DriversLicense {

    private final String number;
    private final LocalDate expiryDate;

    private DriversLicense(String number, LocalDate expiryDate) {
        this.number = number;
        this.expiryDate = expiryDate;
    }

    public static DriversLicense of(String number, LocalDate expiryDate) {
        Objects.requireNonNull(number, "license number must not be null");
        Objects.requireNonNull(expiryDate, "expiry date must not be null");
        if (number.isBlank()) {
            throw new IllegalArgumentException("license number must not be blank");
        }
        return new DriversLicense(number.trim(), expiryDate);
    }

    public String number() {
        return number;
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    public boolean isExpired(LocalDate at) {
        return expiryDate.isBefore(at);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriversLicense that)) return false;
        return number.equals(that.number) && expiryDate.equals(that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, expiryDate);
    }
}
