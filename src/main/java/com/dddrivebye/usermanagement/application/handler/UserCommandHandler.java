package com.dddrivebye.usermanagement.application.handler;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.application.command.ActivateAvailabilityCommand;
import com.dddrivebye.usermanagement.application.command.AddDriverProfileCommand;
import com.dddrivebye.usermanagement.application.command.ApproveDriverProfileCommand;
import com.dddrivebye.usermanagement.application.command.DeactivateAvailabilityCommand;
import com.dddrivebye.usermanagement.application.command.DefineActivityZoneCommand;
import com.dddrivebye.usermanagement.application.command.DefineWorkingZoneCommand;
import com.dddrivebye.usermanagement.application.command.RegisterIndividualAccountCommand;
import com.dddrivebye.usermanagement.application.command.RegisterProfessionalAccountCommand;
import com.dddrivebye.usermanagement.application.command.RegisterVehicleCommand;
import com.dddrivebye.usermanagement.application.command.RejectDriverProfileCommand;
import com.dddrivebye.usermanagement.domain.entity.User;
import com.dddrivebye.usermanagement.domain.entity.VehicleProfile;
import com.dddrivebye.usermanagement.domain.exception.UserNotFoundException;
import com.dddrivebye.usermanagement.domain.repository.UserRepository;
import com.dddrivebye.usermanagement.domain.valueobject.ActivityZone;
import com.dddrivebye.usermanagement.domain.valueobject.DriversLicense;
import com.dddrivebye.usermanagement.domain.valueobject.Email;
import com.dddrivebye.usermanagement.domain.valueobject.FuelType;
import com.dddrivebye.usermanagement.domain.valueobject.FullName;
import com.dddrivebye.usermanagement.domain.valueobject.PhoneNumber;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleInsurance;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleOption;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleRegistration;
import com.dddrivebye.usermanagement.domain.valueobject.VtcLicense;
import com.dddrivebye.usermanagement.domain.valueobject.WorkingZone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserCommandHandler {

    private final UserRepository users;
    private final DomainEventPublisher eventPublisher;

    public UserCommandHandler(UserRepository users, DomainEventPublisher eventPublisher) {
        this.users = users;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UUID handle(RegisterIndividualAccountCommand command) {
        User user = User.registerIndividual(
                FullName.of(command.fullName()),
                Email.of(command.email()),
                PhoneNumber.of(command.phoneNumber()));
        users.save(user);
        publishEvents(user);
        return user.id().value();
    }

    @Transactional
    public UUID handle(RegisterProfessionalAccountCommand command) {
        User user = User.registerProfessional(
                FullName.of(command.fullName()),
                Email.of(command.email()),
                PhoneNumber.of(command.phoneNumber()),
                VtcLicense.of(command.vtcLicenseNumber(), command.vtcLicenseExpiry()),
                DriversLicense.of(command.driversLicenseNumber(), command.driversLicenseExpiry()),
                VehicleInsurance.of(command.insuranceCompany(), command.insurancePolicyNumber(), command.insuranceExpiry()),
                VehicleRegistration.of(command.vehicleMake(), command.vehicleModel(), command.vehicleYear(), command.vehicleLicensePlate()));
        users.save(user);
        publishEvents(user);
        return user.id().value();
    }

    @Transactional
    public void handle(AddDriverProfileCommand command) {
        User user = loadUser(command.userId());
        user.addIndividualDriverProfile(
                DriversLicense.of(command.driversLicenseNumber(), command.driversLicenseExpiry()),
                VehicleInsurance.of(command.insuranceCompany(), command.insurancePolicyNumber(), command.insuranceExpiry()),
                VehicleRegistration.of(command.vehicleMake(), command.vehicleModel(), command.vehicleYear(), command.vehicleLicensePlate()));
        users.save(user);
        publishEvents(user);
    }

    @Transactional
    public void handle(ApproveDriverProfileCommand command) {
        User user = loadUser(command.userId());
        user.approveDriverProfile();
        users.save(user);
        publishEvents(user);
    }

    @Transactional
    public void handle(RejectDriverProfileCommand command) {
        User user = loadUser(command.userId());
        user.rejectDriverProfile(command.reason());
        users.save(user);
        publishEvents(user);
    }

    @Transactional
    public UUID handle(RegisterVehicleCommand command) {
        User user = loadUser(command.userId());
        Set<VehicleOption> options = command.options() == null
                ? EnumSet.noneOf(VehicleOption.class)
                : command.options().stream().map(VehicleOption::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(VehicleOption.class)));
        VehicleProfile vehicle = VehicleProfile.create(
                VehicleRegistration.of(command.make(), command.model(), command.year(), command.licensePlate()),
                command.seats(),
                FuelType.valueOf(command.fuelType()),
                options);
        user.registerVehicle(vehicle);
        users.save(user);
        publishEvents(user);
        return vehicle.id().value();
    }

    @Transactional
    public void handle(ActivateAvailabilityCommand command) {
        User user = loadUser(command.userId());
        user.activateAvailability(command.hasActivePassengerRide());
        users.save(user);
        publishEvents(user);
    }

    @Transactional
    public void handle(DeactivateAvailabilityCommand command) {
        User user = loadUser(command.userId());
        user.deactivateAvailability();
        users.save(user);
        publishEvents(user);
    }

    @Transactional
    public void handle(DefineActivityZoneCommand command) {
        User user = loadUser(command.userId());
        user.defineActivityZone(ActivityZone.of(
                command.label(),
                GeoCoordinates.of(command.latitude(), command.longitude()),
                command.radiusKm()));
        users.save(user);
        publishEvents(user);
    }

    @Transactional
    public void handle(DefineWorkingZoneCommand command) {
        User user = loadUser(command.userId());
        user.defineWorkingZone(WorkingZone.of(
                command.label(),
                GeoCoordinates.of(command.latitude(), command.longitude()),
                command.radiusKm()));
        users.save(user);
        publishEvents(user);
    }

    private User loadUser(UUID id) {
        UserId userId = UserId.of(id);
        return users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void publishEvents(User user) {
        List<BaseDomainEvent> events = user.pullDomainEvents();
        events.forEach(eventPublisher::publish);
    }
}
