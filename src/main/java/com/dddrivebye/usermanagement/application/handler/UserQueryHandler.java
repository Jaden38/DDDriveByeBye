package com.dddrivebye.usermanagement.application.handler;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.api.dto.AvailableDriverDto;
import com.dddrivebye.usermanagement.api.dto.DriverProfileDto;
import com.dddrivebye.usermanagement.api.dto.UserDto;
import com.dddrivebye.usermanagement.application.query.GetAvailableDriversNearQuery;
import com.dddrivebye.usermanagement.application.query.GetDriverProfileQuery;
import com.dddrivebye.usermanagement.application.query.GetUserByIdQuery;
import com.dddrivebye.usermanagement.application.query.IsDriverAvailableQuery;
import com.dddrivebye.usermanagement.domain.entity.DriverProfile;
import com.dddrivebye.usermanagement.domain.entity.User;
import com.dddrivebye.usermanagement.domain.repository.UserRepository;
import com.dddrivebye.usermanagement.domain.valueobject.AccountType;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserQueryHandler {

    private final UserRepository users;

    public UserQueryHandler(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> handle(GetUserByIdQuery query) {
        return users.findById(UserId.of(query.userId())).map(this::toUserDto);
    }

    @Transactional(readOnly = true)
    public Optional<DriverProfileDto> handle(GetDriverProfileQuery query) {
        return users.findById(UserId.of(query.userId()))
                .flatMap(user -> user.driverProfile().map(profile -> toProfileDto(user, profile)));
    }

    @Transactional(readOnly = true)
    public List<AvailableDriverDto> handle(GetAvailableDriversNearQuery query) {
        AccountType type = query.requiredAccountType() == null
                ? null
                : AccountType.valueOf(query.requiredAccountType());
        return users.findAvailableDriversNear(
                        GeoCoordinates.of(query.latitude(), query.longitude()),
                        query.radiusKm(),
                        type)
                .stream()
                .map(user -> {
                    DriverProfile profile = user.driverProfile().orElseThrow();
                    GeoCoordinates center = profile.workingZone()
                            .map(z -> z.center())
                            .orElseGet(() -> profile.activityZone()
                                    .map(z -> z.center())
                                    .orElse(GeoCoordinates.of(query.latitude(), query.longitude())));
                    return new AvailableDriverDto(
                            user.id().value(),
                            user.type().name(),
                            center.latitude(),
                            center.longitude());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean handle(IsDriverAvailableQuery query) {
        return users.findById(UserId.of(query.userId()))
                .flatMap(User::driverProfile)
                .map(p -> p.availability().isAvailable())
                .orElse(false);
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.id().value(),
                user.fullName().value(),
                user.email().value(),
                user.phoneNumber().value(),
                user.type().name(),
                user.status().name());
    }

    private DriverProfileDto toProfileDto(User user, DriverProfile profile) {
        List<DriverProfileDto.VehicleDto> vehicles = profile.vehicles().stream()
                .map(v -> new DriverProfileDto.VehicleDto(
                        v.id().value(),
                        v.registration().make(),
                        v.registration().model(),
                        v.registration().year(),
                        v.registration().licensePlate(),
                        v.seats(),
                        v.fuelType().name(),
                        v.options().stream().map(Enum::name).toList()))
                .toList();
        return new DriverProfileDto(
                user.id().value(),
                user.type().name(),
                profile.status().name(),
                profile.availability().status().name(),
                profile.workingZone().map(z -> z.label()).orElse(null),
                profile.activityZone().map(z -> z.label()).orElse(null),
                vehicles);
    }
}
