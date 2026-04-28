package com.dddrivebye.usermanagement.domain.entity;

import com.dddrivebye.usermanagement.domain.valueobject.FuelType;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleOption;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleProfileId;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleRegistration;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class VehicleProfile {

    private final VehicleProfileId id;
    private VehicleRegistration registration;
    private int seats;
    private FuelType fuelType;
    private final Set<VehicleOption> options;

    private VehicleProfile(VehicleProfileId id,
                           VehicleRegistration registration,
                           int seats,
                           FuelType fuelType,
                           Set<VehicleOption> options) {
        this.id = id;
        this.registration = registration;
        this.seats = seats;
        this.fuelType = fuelType;
        this.options = options;
    }

    public static VehicleProfile create(VehicleRegistration registration,
                                        int seats,
                                        FuelType fuelType,
                                        Set<VehicleOption> options) {
        Objects.requireNonNull(registration, "registration must not be null");
        Objects.requireNonNull(fuelType, "fuel type must not be null");
        if (seats < 1 || seats > 9) {
            throw new IllegalArgumentException("seats must be between 1 and 9: " + seats);
        }
        EnumSet<VehicleOption> safeOptions = options == null
                ? EnumSet.noneOf(VehicleOption.class)
                : EnumSet.copyOf(options);
        return new VehicleProfile(VehicleProfileId.generate(), registration, seats, fuelType, safeOptions);
    }

    public static VehicleProfile reconstitute(VehicleProfileId id,
                                              VehicleRegistration registration,
                                              int seats,
                                              FuelType fuelType,
                                              Set<VehicleOption> options) {
        return new VehicleProfile(id, registration, seats, fuelType,
                options == null ? EnumSet.noneOf(VehicleOption.class) : EnumSet.copyOf(options));
    }

    public VehicleProfileId id() {
        return id;
    }

    public VehicleRegistration registration() {
        return registration;
    }

    public int seats() {
        return seats;
    }

    public FuelType fuelType() {
        return fuelType;
    }

    public Set<VehicleOption> options() {
        return EnumSet.copyOf(options);
    }

    public boolean supports(Set<VehicleOption> required) {
        return options.containsAll(required);
    }
}
