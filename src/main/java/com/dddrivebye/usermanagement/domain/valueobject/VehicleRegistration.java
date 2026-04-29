package com.dddrivebye.usermanagement.domain.valueobject;

import java.util.Objects;

public final class VehicleRegistration {

    private final String make;
    private final String model;
    private final int year;
    private final String licensePlate;

    private VehicleRegistration(String make, String model, int year, String licensePlate) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
    }

    public static VehicleRegistration of(String make, String model, int year, String licensePlate) {
        Objects.requireNonNull(make, "make must not be null");
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(licensePlate, "license plate must not be null");
        if (make.isBlank() || model.isBlank() || licensePlate.isBlank()) {
            throw new IllegalArgumentException("registration fields must not be blank");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("year out of range: " + year);
        }
        return new VehicleRegistration(make.trim(), model.trim(), year, licensePlate.trim().toUpperCase());
    }

    public String make() {
        return make;
    }

    public String model() {
        return model;
    }

    public int year() {
        return year;
    }

    public String licensePlate() {
        return licensePlate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleRegistration that)) return false;
        return year == that.year
                && make.equals(that.make)
                && model.equals(that.model)
                && licensePlate.equals(that.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(make, model, year, licensePlate);
    }
}
