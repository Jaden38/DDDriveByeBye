package com.dddrivebye.usermanagement.infrastructure.persistence;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.entity.Availability;
import com.dddrivebye.usermanagement.domain.entity.DriverProfile;
import com.dddrivebye.usermanagement.domain.entity.User;
import com.dddrivebye.usermanagement.domain.entity.VehicleProfile;
import com.dddrivebye.usermanagement.domain.repository.UserRepository;
import com.dddrivebye.usermanagement.domain.valueobject.AccountStatus;
import com.dddrivebye.usermanagement.domain.valueobject.AccountType;
import com.dddrivebye.usermanagement.domain.valueobject.ActivityZone;
import com.dddrivebye.usermanagement.domain.valueobject.AvailabilityStatus;
import com.dddrivebye.usermanagement.domain.valueobject.DriverProfileStatus;
import com.dddrivebye.usermanagement.domain.valueobject.DriversLicense;
import com.dddrivebye.usermanagement.domain.valueobject.Email;
import com.dddrivebye.usermanagement.domain.valueobject.FuelType;
import com.dddrivebye.usermanagement.domain.valueobject.FullName;
import com.dddrivebye.usermanagement.domain.valueobject.PhoneNumber;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleInsurance;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleOption;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleProfileId;
import com.dddrivebye.usermanagement.domain.valueobject.VehicleRegistration;
import com.dddrivebye.usermanagement.domain.valueobject.VtcLicense;
import com.dddrivebye.usermanagement.domain.valueobject.WorkingZone;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository delegate;

    public JpaUserRepository(SpringDataUserRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = delegate.findById(user.id().value()).orElseGet(UserJpaEntity::new);
        toJpa(user, entity);
        delegate.save(entity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return delegate.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return delegate.findByEmail(email.toLowerCase()).map(this::toDomain);
    }

    @Override
    public List<User> findAvailableDriversNear(GeoCoordinates point, double radiusKm, AccountType requiredType) {
        String accountType = requiredType == null ? null : requiredType.name();
        return delegate.findAvailableDrivers(accountType).stream()
                .map(this::toDomain)
                .filter(u -> u.driverProfile().map(p -> withinZone(p, point, radiusKm)).orElse(false))
                .collect(Collectors.toList());
    }

    private boolean withinZone(DriverProfile profile, GeoCoordinates point, double radiusKm) {
        if (profile.workingZone().isPresent()) {
            return profile.workingZone().get().covers(point);
        }
        if (profile.activityZone().isPresent()) {
            return profile.activityZone().get().center().distanceKmTo(point) <= radiusKm;
        }
        return true;
    }

    private void toJpa(User user, UserJpaEntity entity) {
        entity.setId(user.id().value());
        entity.setFullName(user.fullName().value());
        entity.setEmail(user.email().value());
        entity.setPhoneNumber(user.phoneNumber().value());
        entity.setAccountType(user.type().name());
        entity.setAccountStatus(user.status().name());

        user.driverProfile().ifPresentOrElse(
                profile -> applyDriverProfile(profile, entity),
                () -> clearDriverProfile(entity));
    }

    private void applyDriverProfile(DriverProfile profile, UserJpaEntity entity) {
        entity.setDriverProfileStatus(profile.status().name());
        entity.setDriversLicenseNumber(profile.driversLicense().number());
        entity.setDriversLicenseExpiry(profile.driversLicense().expiryDate());
        entity.setVtcLicenseNumber(profile.vtcLicense().map(VtcLicense::number).orElse(null));
        entity.setVtcLicenseExpiry(profile.vtcLicense().map(VtcLicense::expiryDate).orElse(null));
        entity.setInsuranceCompany(profile.insurance().company());
        entity.setInsurancePolicyNumber(profile.insurance().policyNumber());
        entity.setInsuranceExpiry(profile.insurance().expiryDate());
        entity.setVehicleMake(profile.registration().make());
        entity.setVehicleModel(profile.registration().model());
        entity.setVehicleYear(profile.registration().year());
        entity.setVehicleLicensePlate(profile.registration().licensePlate());
        entity.setAvailabilityStatus(profile.availability().status().name());
        entity.setRejectionReason(profile.rejectionReason().orElse(null));

        profile.activityZone().ifPresentOrElse(z -> {
            entity.setActivityZoneLabel(z.label());
            entity.setActivityZoneLat(z.center().latitude());
            entity.setActivityZoneLon(z.center().longitude());
            entity.setActivityZoneRadiusKm(z.radiusKm());
        }, () -> {
            entity.setActivityZoneLabel(null);
            entity.setActivityZoneLat(null);
            entity.setActivityZoneLon(null);
            entity.setActivityZoneRadiusKm(null);
        });

        profile.workingZone().ifPresentOrElse(z -> {
            entity.setWorkingZoneLabel(z.label());
            entity.setWorkingZoneLat(z.center().latitude());
            entity.setWorkingZoneLon(z.center().longitude());
            entity.setWorkingZoneRadiusKm(z.radiusKm());
        }, () -> {
            entity.setWorkingZoneLabel(null);
            entity.setWorkingZoneLat(null);
            entity.setWorkingZoneLon(null);
            entity.setWorkingZoneRadiusKm(null);
        });

        entity.getVehicles().clear();
        for (VehicleProfile v : profile.vehicles()) {
            VehicleJpaEntity vEntity = new VehicleJpaEntity();
            vEntity.setId(v.id().value());
            vEntity.setUser(entity);
            vEntity.setMake(v.registration().make());
            vEntity.setModel(v.registration().model());
            vEntity.setYear(v.registration().year());
            vEntity.setLicensePlate(v.registration().licensePlate());
            vEntity.setSeats(v.seats());
            vEntity.setFuelType(v.fuelType().name());
            vEntity.setOptions(v.options().stream().map(Enum::name).collect(Collectors.toSet()));
            entity.getVehicles().add(vEntity);
        }
    }

    private void clearDriverProfile(UserJpaEntity entity) {
        entity.setDriverProfileStatus(null);
        entity.setDriversLicenseNumber(null);
        entity.setDriversLicenseExpiry(null);
        entity.setVtcLicenseNumber(null);
        entity.setVtcLicenseExpiry(null);
        entity.setInsuranceCompany(null);
        entity.setInsurancePolicyNumber(null);
        entity.setInsuranceExpiry(null);
        entity.setVehicleMake(null);
        entity.setVehicleModel(null);
        entity.setVehicleYear(null);
        entity.setVehicleLicensePlate(null);
        entity.setAvailabilityStatus(null);
        entity.setActivityZoneLabel(null);
        entity.setActivityZoneLat(null);
        entity.setActivityZoneLon(null);
        entity.setActivityZoneRadiusKm(null);
        entity.setWorkingZoneLabel(null);
        entity.setWorkingZoneLat(null);
        entity.setWorkingZoneLon(null);
        entity.setWorkingZoneRadiusKm(null);
        entity.setRejectionReason(null);
        entity.getVehicles().clear();
    }

    private User toDomain(UserJpaEntity entity) {
        DriverProfile profile = entity.getDriverProfileStatus() == null ? null : reconstituteProfile(entity);
        return User.reconstitute(
                UserId.of(entity.getId()),
                FullName.of(entity.getFullName()),
                Email.of(entity.getEmail()),
                PhoneNumber.of(entity.getPhoneNumber()),
                AccountType.valueOf(entity.getAccountType()),
                AccountStatus.valueOf(entity.getAccountStatus()),
                profile);
    }

    private DriverProfile reconstituteProfile(UserJpaEntity entity) {
        DriversLicense driversLicense = DriversLicense.of(entity.getDriversLicenseNumber(), entity.getDriversLicenseExpiry());
        VtcLicense vtcLicense = entity.getVtcLicenseNumber() == null
                ? null
                : VtcLicense.of(entity.getVtcLicenseNumber(), entity.getVtcLicenseExpiry());
        VehicleInsurance insurance = VehicleInsurance.of(
                entity.getInsuranceCompany(),
                entity.getInsurancePolicyNumber(),
                entity.getInsuranceExpiry());
        VehicleRegistration registration = VehicleRegistration.of(
                entity.getVehicleMake(),
                entity.getVehicleModel(),
                entity.getVehicleYear(),
                entity.getVehicleLicensePlate());
        Availability availability = Availability.reconstitute(
                AvailabilityStatus.valueOf(entity.getAvailabilityStatus()));
        ActivityZone activityZone = entity.getActivityZoneLabel() == null
                ? null
                : ActivityZone.of(
                    entity.getActivityZoneLabel(),
                    GeoCoordinates.of(entity.getActivityZoneLat(), entity.getActivityZoneLon()),
                    entity.getActivityZoneRadiusKm());
        WorkingZone workingZone = entity.getWorkingZoneLabel() == null
                ? null
                : WorkingZone.of(
                    entity.getWorkingZoneLabel(),
                    GeoCoordinates.of(entity.getWorkingZoneLat(), entity.getWorkingZoneLon()),
                    entity.getWorkingZoneRadiusKm());
        List<VehicleProfile> vehicles = new ArrayList<>();
        for (VehicleJpaEntity v : entity.getVehicles()) {
            Set<VehicleOption> options = v.getOptions().stream()
                    .map(VehicleOption::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(VehicleOption.class)));
            vehicles.add(VehicleProfile.reconstitute(
                    VehicleProfileId.of(v.getId()),
                    VehicleRegistration.of(v.getMake(), v.getModel(), v.getYear(), v.getLicensePlate()),
                    v.getSeats(),
                    FuelType.valueOf(v.getFuelType()),
                    options));
        }
        return DriverProfile.reconstitute(
                DriverProfileStatus.valueOf(entity.getDriverProfileStatus()),
                driversLicense,
                vtcLicense,
                insurance,
                registration,
                vehicles,
                availability,
                activityZone,
                workingZone,
                entity.getRejectionReason());
    }
}
