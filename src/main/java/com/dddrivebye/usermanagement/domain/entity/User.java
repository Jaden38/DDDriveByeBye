package com.dddrivebye.usermanagement.domain.entity;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.usermanagement.domain.event.AccountRestrictedEvent;
import com.dddrivebye.usermanagement.domain.event.DriverAvailabilityChangedEvent;
import com.dddrivebye.usermanagement.domain.event.DriverProfileRejectedEvent;
import com.dddrivebye.usermanagement.domain.event.DriverProfileValidatedEvent;
import com.dddrivebye.usermanagement.domain.exception.InvalidAccountOperationException;
import com.dddrivebye.usermanagement.domain.valueobject.AccountStatus;
import com.dddrivebye.usermanagement.domain.valueobject.AccountType;
import com.dddrivebye.usermanagement.domain.valueobject.ActivityZone;
import com.dddrivebye.usermanagement.domain.valueobject.AvailabilityStatus;
import com.dddrivebye.usermanagement.domain.valueobject.DriverProfileStatus;
import com.dddrivebye.usermanagement.domain.valueobject.DriversLicense;
import com.dddrivebye.usermanagement.domain.valueobject.Email;
import com.dddrivebye.usermanagement.domain.valueobject.FullName;
import com.dddrivebye.usermanagement.domain.valueobject.PhoneNumber;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleInsurance;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleRegistration;
import com.dddrivebye.usermanagement.domain.valueobject.VtcLicense;
import com.dddrivebye.usermanagement.domain.valueobject.WorkingZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class User {

    private final UserId id;
    private FullName fullName;
    private final Email email;
    private PhoneNumber phoneNumber;
    private final AccountType type;
    private AccountStatus status;
    private DriverProfile driverProfile;
    private final List<BaseDomainEvent> domainEvents = new ArrayList<>();

    private User(UserId id,
                 FullName fullName,
                 Email email,
                 PhoneNumber phoneNumber,
                 AccountType type,
                 AccountStatus status,
                 DriverProfile driverProfile) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.status = status;
        this.driverProfile = driverProfile;
    }

    public static User registerIndividual(FullName fullName, Email email, PhoneNumber phoneNumber) {
        return new User(UserId.generate(), fullName, email, phoneNumber,
                AccountType.INDIVIDUAL, AccountStatus.ACTIVE, null);
    }

    public static User registerProfessional(FullName fullName,
                                            Email email,
                                            PhoneNumber phoneNumber,
                                            VtcLicense vtcLicense,
                                            DriversLicense driversLicense,
                                            VehicleInsurance insurance,
                                            VehicleRegistration registration) {
        DriverProfile profile = DriverProfile.newApplicationForProfessional(
                driversLicense, vtcLicense, insurance, registration);
        return new User(UserId.generate(), fullName, email, phoneNumber,
                AccountType.PROFESSIONAL, AccountStatus.PENDING_VALIDATION, profile);
    }

    public static User reconstitute(UserId id,
                                    FullName fullName,
                                    Email email,
                                    PhoneNumber phoneNumber,
                                    AccountType type,
                                    AccountStatus status,
                                    DriverProfile driverProfile) {
        return new User(id, fullName, email, phoneNumber, type, status, driverProfile);
    }

    public UserId id() {
        return id;
    }

    public FullName fullName() {
        return fullName;
    }

    public Email email() {
        return email;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public AccountType type() {
        return type;
    }

    public AccountStatus status() {
        return status;
    }

    public Optional<DriverProfile> driverProfile() {
        return Optional.ofNullable(driverProfile);
    }

    public boolean canActAsPassenger() {
        return type == AccountType.INDIVIDUAL && status == AccountStatus.ACTIVE;
    }

    public boolean canActAsDriver() {
        return driverProfile != null && driverProfile.isActive() && status == AccountStatus.ACTIVE;
    }

    public boolean canPublishRideOffer() {
        return type == AccountType.INDIVIDUAL && canActAsDriver();
    }

    public boolean canHandleScheduledRide() {
        return type == AccountType.INDIVIDUAL;
    }

    public void addIndividualDriverProfile(DriversLicense driversLicense,
                                           VehicleInsurance insurance,
                                           VehicleRegistration registration) {
        ensureType(AccountType.INDIVIDUAL, "Only Individual accounts may add a driver profile");
        if (driverProfile != null && driverProfile.status() != DriverProfileStatus.REJECTED) {
            throw new InvalidAccountOperationException("Driver profile already submitted");
        }
        if (driverProfile != null) {
            driverProfile.resubmit(driversLicense, insurance, registration);
        } else {
            driverProfile = DriverProfile.newApplicationForIndividual(driversLicense, insurance, registration);
        }
    }

    public void approveDriverProfile() {
        ensureDriverProfile();
        driverProfile.approve();
        if (type == AccountType.PROFESSIONAL) {
            status = AccountStatus.ACTIVE;
        }
        domainEvents.add(new DriverProfileValidatedEvent(id));
    }

    public void rejectDriverProfile(String reason) {
        ensureDriverProfile();
        driverProfile.reject(reason);
        domainEvents.add(new DriverProfileRejectedEvent(id, reason));
    }

    public void registerVehicle(com.dddrivebye.usermanagement.domain.entity.VehicleProfile vehicle) {
        ensureDriverProfile();
        driverProfile.registerVehicle(vehicle);
    }

    public void updateInsurance(VehicleInsurance updated) {
        ensureDriverProfile();
        driverProfile.updateInsurance(updated);
    }

    public void defineActivityZone(ActivityZone zone) {
        ensureType(AccountType.INDIVIDUAL, "Activity zone is only for Individual accounts");
        ensureDriverProfile();
        driverProfile.defineActivityZone(zone);
    }

    public void defineWorkingZone(WorkingZone zone) {
        ensureType(AccountType.PROFESSIONAL, "Working zone is only for Professional accounts");
        ensureDriverProfile();
        driverProfile.defineWorkingZone(zone);
    }

    public void activateAvailability(boolean hasActivePassengerRide) {
        ensureDriverProfile();
        if (!driverProfile.isActive()) {
            throw new InvalidAccountOperationException(
                    "Driver profile must be ACTIVE to activate availability");
        }
        if (status != AccountStatus.ACTIVE) {
            throw new InvalidAccountOperationException(
                    "Account must be ACTIVE to activate availability, current=" + status);
        }
        if (!driverProfile.hasValidVehicle()) {
            throw new InvalidAccountOperationException(
                    "A valid vehicle profile is required to activate availability");
        }
        if (type == AccountType.INDIVIDUAL && hasActivePassengerRide) {
            throw new InvalidAccountOperationException(
                    "You cannot activate driver availability while you have an active ride as a passenger");
        }
        if (type == AccountType.PROFESSIONAL && driverProfile.workingZone().isEmpty()) {
            throw new InvalidAccountOperationException(
                    "You must define a Working Zone before activating your availability");
        }
        AvailabilityStatus previous = driverProfile.availability().activate();
        if (previous != AvailabilityStatus.AVAILABLE) {
            domainEvents.add(new DriverAvailabilityChangedEvent(id, previous, AvailabilityStatus.AVAILABLE));
        }
    }

    public void deactivateAvailability() {
        ensureDriverProfile();
        AvailabilityStatus previous = driverProfile.availability().deactivate();
        if (previous != AvailabilityStatus.OFFLINE) {
            domainEvents.add(new DriverAvailabilityChangedEvent(id, previous, AvailabilityStatus.OFFLINE));
        }
    }

    public void restrict() {
        if (status == AccountStatus.RESTRICTED) {
            return;
        }
        status = AccountStatus.RESTRICTED;
        domainEvents.add(new AccountRestrictedEvent(id));
    }

    public void liftRestriction() {
        if (status == AccountStatus.RESTRICTED) {
            status = AccountStatus.ACTIVE;
        }
    }

    public List<BaseDomainEvent> pullDomainEvents() {
        List<BaseDomainEvent> snapshot = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(snapshot);
    }

    private void ensureType(AccountType expected, String message) {
        if (this.type != expected) {
            throw new InvalidAccountOperationException(message);
        }
    }

    private void ensureDriverProfile() {
        if (driverProfile == null) {
            throw new InvalidAccountOperationException("No driver profile registered");
        }
    }
}
