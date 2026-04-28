package com.dddrivebye.usermanagement.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

public final class VehicleInsurance {

    private final String company;
    private final String policyNumber;
    private final LocalDate expiryDate;

    private VehicleInsurance(String company, String policyNumber, LocalDate expiryDate) {
        this.company = company;
        this.policyNumber = policyNumber;
        this.expiryDate = expiryDate;
    }

    public static VehicleInsurance of(String company, String policyNumber, LocalDate expiryDate) {
        Objects.requireNonNull(company, "company must not be null");
        Objects.requireNonNull(policyNumber, "policy number must not be null");
        Objects.requireNonNull(expiryDate, "expiry date must not be null");
        if (company.isBlank() || policyNumber.isBlank()) {
            throw new IllegalArgumentException("insurance fields must not be blank");
        }
        return new VehicleInsurance(company.trim(), policyNumber.trim(), expiryDate);
    }

    public String company() {
        return company;
    }

    public String policyNumber() {
        return policyNumber;
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    public VehicleInsurance withExpiryDate(LocalDate newDate) {
        return new VehicleInsurance(company, policyNumber, newDate);
    }

    public boolean expiresWithin(LocalDate referenceDate, int days) {
        return !expiryDate.isAfter(referenceDate.plusDays(days));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleInsurance that)) return false;
        return company.equals(that.company)
                && policyNumber.equals(that.policyNumber)
                && expiryDate.equals(that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, policyNumber, expiryDate);
    }
}
