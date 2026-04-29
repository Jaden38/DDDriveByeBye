package com.dddrivebye.usermanagement.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

public final class VtcLicense {

    private final String number;
    private final LocalDate expiryDate;

    private VtcLicense(String number, LocalDate expiryDate) {
        this.number = number;
        this.expiryDate = expiryDate;
    }

    public static VtcLicense of(String number, LocalDate expiryDate) {
        Objects.requireNonNull(number, "VTC license number must not be null");
        Objects.requireNonNull(expiryDate, "expiry date must not be null");
        if (number.isBlank()) {
            throw new IllegalArgumentException("VTC license number must not be blank");
        }
        return new VtcLicense(number.trim(), expiryDate);
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
        if (!(o instanceof VtcLicense that)) return false;
        return number.equals(that.number) && expiryDate.equals(that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, expiryDate);
    }
}
