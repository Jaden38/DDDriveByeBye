package com.dddrivebye.usermanagement.api;

import com.dddrivebye.usermanagement.api.dto.AvailableDriverDto;
import com.dddrivebye.usermanagement.api.dto.DriverProfileDto;
import com.dddrivebye.usermanagement.api.dto.UserDto;
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
import com.dddrivebye.usermanagement.application.handler.UserCommandHandler;
import com.dddrivebye.usermanagement.application.handler.UserQueryHandler;
import com.dddrivebye.usermanagement.application.query.GetAvailableDriversNearQuery;
import com.dddrivebye.usermanagement.application.query.GetDriverProfileQuery;
import com.dddrivebye.usermanagement.application.query.GetUserByIdQuery;
import com.dddrivebye.usermanagement.application.query.IsDriverAvailableQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the user-management bounded context.
 * Other modules MUST depend on this class only — never on internal handlers, repositories or domain types.
 */
@Component
public class UserManagementFacade {

    private final UserCommandHandler commandHandler;
    private final UserQueryHandler queryHandler;

    public UserManagementFacade(UserCommandHandler commandHandler, UserQueryHandler queryHandler) {
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
    }

    public UUID registerIndividualAccount(RegisterIndividualAccountCommand command) {
        return commandHandler.handle(command);
    }

    public UUID registerProfessionalAccount(RegisterProfessionalAccountCommand command) {
        return commandHandler.handle(command);
    }

    public void addDriverProfile(AddDriverProfileCommand command) {
        commandHandler.handle(command);
    }

    public void approveDriverProfile(UUID userId) {
        commandHandler.handle(new ApproveDriverProfileCommand(userId));
    }

    public void rejectDriverProfile(UUID userId, String reason) {
        commandHandler.handle(new RejectDriverProfileCommand(userId, reason));
    }

    public UUID registerVehicle(RegisterVehicleCommand command) {
        return commandHandler.handle(command);
    }

    public void activateAvailability(UUID userId, boolean hasActivePassengerRide) {
        commandHandler.handle(new ActivateAvailabilityCommand(userId, hasActivePassengerRide));
    }

    public void deactivateAvailability(UUID userId) {
        commandHandler.handle(new DeactivateAvailabilityCommand(userId));
    }

    public void defineActivityZone(DefineActivityZoneCommand command) {
        commandHandler.handle(command);
    }

    public void defineWorkingZone(DefineWorkingZoneCommand command) {
        commandHandler.handle(command);
    }

    public Optional<UserDto> getUserById(UUID userId) {
        return queryHandler.handle(new GetUserByIdQuery(userId));
    }

    public Optional<DriverProfileDto> getDriverProfile(UUID userId) {
        return queryHandler.handle(new GetDriverProfileQuery(userId));
    }

    public List<AvailableDriverDto> getAvailableDriversNear(double latitude,
                                                            double longitude,
                                                            double radiusKm,
                                                            String requiredAccountType) {
        return queryHandler.handle(new GetAvailableDriversNearQuery(latitude, longitude, radiusKm, requiredAccountType));
    }

    public boolean isDriverAvailable(UUID userId) {
        return queryHandler.handle(new IsDriverAvailableQuery(userId));
    }
}
