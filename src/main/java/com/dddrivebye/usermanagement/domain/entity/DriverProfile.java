package com.dddrivebye.usermanagement.domain.entity;

import com.dddrivebye.usermanagement.domain.exception.InvalidAccountOperationException;
import com.dddrivebye.usermanagement.domain.valueobject.ActivityZone;
import com.dddrivebye.usermanagement.domain.valueobject.DriverProfileStatus;
import com.dddrivebye.usermanagement.domain.valueobject.DriversLicense;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleInsurance;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleProfileId;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleRegistration;
import com.dddrivebye.usermanagement.domain.valueobject.VtcLicense;
import com.dddrivebye.usermanagement.domain.valueobject.WorkingZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DriverProfile {

    private DriverProfileStatus status;
    private DriversLicense driversLicense;
    private VtcLicense vtcLicense;
    private VehicleInsurance insurance;
    private VehicleRegistration registration;
    private final List<VehicleProfile> vehicles;
    private final Availability availability;
    private ActivityZone activityZone;
    private WorkingZone workingZone;
    private String rejectionReason;

    private DriverProfile(DriverProfileStatus status,
                          DriversLicense driversLicense,
                          VtcLicense vtcLicense,
                          VehicleInsurance insurance,
                          VehicleRegistration registration,
                          List<VehicleProfile> vehicles,
                          Availability availability,
                          ActivityZone activityZone,
                          WorkingZone workingZone,
                          String rejectionReason) {
        this.status = status;
        this.driversLicense = driversLicense;
        this.vtcLicense = vtcLicense;
        this.insurance = insurance;
        this.registration = registration;
        this.vehicles = vehicles;
        this.availability = availability;
        this.activityZone = activityZone;
        this.workingZone = workingZone;
        this.rejectionReason = rejectionReason;
    }

    public static DriverProfile newApplicationForIndividual(DriversLicense driversLicense,
                                                            VehicleInsurance insurance,
                                                            VehicleRegistration registration) {
        return new DriverProfile(
                DriverProfileStatus.PENDING_VALIDATION,
                driversLicense,
                null,
                insurance,
                registration,
                new ArrayList<>(),
                Availability.offline(),
                null,
                null,
                null);
    }

    public static DriverProfile newApplicationForProfessional(DriversLicense driversLicense,
                                                              VtcLicense vtcLicense,
                                                              VehicleInsurance insurance,
                                                              VehicleRegistration registration) {
        return new DriverProfile(
                DriverProfileStatus.PENDING_VALIDATION,
                driversLicense,
                vtcLicense,
                insurance,
                registration,
                new ArrayList<>(),
                Availability.offline(),
                null,
                null,
                null);
    }

    public static DriverProfile reconstitute(DriverProfileStatus status,
                                             DriversLicense driversLicense,
                                             VtcLicense vtcLicense,
                                             VehicleInsurance insurance,
                                             VehicleRegistration registration,
                                             List<VehicleProfile> vehicles,
                                             Availability availability,
                                             ActivityZone activityZone,
                                             WorkingZone workingZone,
                                             String rejectionReason) {
        return new DriverProfile(status, driversLicense, vtcLicense, insurance, registration,
                vehicles == null ? new ArrayList<>() : new ArrayList<>(vehicles),
                availability == null ? Availability.offline() : availability,
                activityZone, workingZone, rejectionReason);
    }

    public DriverProfileStatus status() {
        return status;
    }

    public DriversLicense driversLicense() {
        return driversLicense;
    }

    public Optional<VtcLicense> vtcLicense() {
        return Optional.ofNullable(vtcLicense);
    }

    public VehicleInsurance insurance() {
        return insurance;
    }

    public VehicleRegistration registration() {
        return registration;
    }

    public List<VehicleProfile> vehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public Availability availability() {
        return availability;
    }

    public Optional<ActivityZone> activityZone() {
        return Optional.ofNullable(activityZone);
    }

    public Optional<WorkingZone> workingZone() {
        return Optional.ofNullable(workingZone);
    }

    public Optional<String> rejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }

    public void approve() {
        if (status != DriverProfileStatus.PENDING_VALIDATION) {
            throw new InvalidAccountOperationException(
                    "Driver profile can only be approved from PENDING_VALIDATION, current=" + status);
        }
        status = DriverProfileStatus.ACTIVE;
        rejectionReason = null;
    }

    public void reject(String reason) {
        if (status != DriverProfileStatus.PENDING_VALIDATION) {
            throw new InvalidAccountOperationException(
                    "Driver profile can only be rejected from PENDING_VALIDATION, current=" + status);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("rejection reason must be provided");
        }
        status = DriverProfileStatus.REJECTED;
        rejectionReason = reason.trim();
    }

    public void resubmit(DriversLicense driversLicense,
                         VehicleInsurance insurance,
                         VehicleRegistration registration) {
        if (status != DriverProfileStatus.REJECTED) {
            throw new InvalidAccountOperationException(
                    "Resubmit only allowed from REJECTED, current=" + status);
        }
        this.driversLicense = driversLicense;
        this.insurance = insurance;
        this.registration = registration;
        this.status = DriverProfileStatus.PENDING_VALIDATION;
        this.rejectionReason = null;
    }

    public void registerVehicle(VehicleProfile vehicle) {
        ensureActive();
        vehicles.add(vehicle);
    }

    public void removeVehicle(VehicleProfileId id) {
        vehicles.removeIf(v -> v.id().equals(id));
    }

    public void updateInsurance(VehicleInsurance updated) {
        this.insurance = updated;
    }

    public void defineActivityZone(ActivityZone zone) {
        if (workingZone != null) {
            throw new InvalidAccountOperationException("Professional accounts use Working Zone, not Activity Zone");
        }
        this.activityZone = zone;
    }

    public void defineWorkingZone(WorkingZone zone) {
        if (activityZone != null) {
            throw new InvalidAccountOperationException("Individual accounts use Activity Zone, not Working Zone");
        }
        this.workingZone = zone;
    }

    public boolean isActive() {
        return status == DriverProfileStatus.ACTIVE;
    }

    public boolean hasValidVehicle() {
        return !vehicles.isEmpty();
    }

    private void ensureActive() {
        if (status != DriverProfileStatus.ACTIVE) {
            throw new InvalidAccountOperationException("Driver profile must be ACTIVE, current=" + status);
        }
    }
}
