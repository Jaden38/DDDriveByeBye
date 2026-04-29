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

    public Optional<RideDto> getRideById(UUID rideId) {
        return queryHandler.handle(new GetRideByIdQuery(rideId));
    }
}
