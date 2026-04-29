package com.dddrivebye.ridemanagement.api;

import com.dddrivebye.ridemanagement.api.dto.RideDto;
import com.dddrivebye.ridemanagement.application.command.RequestRideCommand;
import com.dddrivebye.ridemanagement.application.handler.RideCommandHandler;
import com.dddrivebye.ridemanagement.application.handler.RideQueryHandler;
import com.dddrivebye.ridemanagement.application.query.GetRideByIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RideManagementFacade {

    private final RideCommandHandler commandHandler;
    private final RideQueryHandler queryHandler;

    public UUID requestRide(RequestRideCommand command) {
        return commandHandler.handle(command);
    }

    public void proposeDriver(UUID rideId, UUID driverId) {
        commandHandler.handle(new ProposeDriverCommand(rideId, driverId));
    }

    public void acceptRide(UUID rideId) {
        commandHandler.handle(new AcceptRideCommand(rideId));
    }

    public void pickUpPassenger(UUID rideId) {
        commandHandler.handle(new PickUpPassengerCommand(rideId));
    }

    public void startRide(UUID rideId) {
        commandHandler.handle(new StartRideCommand(rideId));
    }

    public void arriveAtDestination(UUID rideId) {
        commandHandler.handle(new ArriveAtDestinationCommand(rideId));
    }

    public void finalizeRide(UUID rideId) {
        commandHandler.handle(new FinalizeRideCommand(rideId));
    }

    public Optional<RideDto> getRideById(UUID rideId) {
        return queryHandler.handle(new GetRideByIdQuery(rideId));
    }
}
