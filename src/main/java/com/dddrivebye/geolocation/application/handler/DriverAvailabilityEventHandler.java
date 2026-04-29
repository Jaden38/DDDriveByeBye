package com.dddrivebye.geolocation.application.handler;

import com.dddrivebye.geolocation.application.command.RemoveDriverPositionCommand;
import com.dddrivebye.usermanagement.domain.event.DriverAvailabilityChangedEvent;
import com.dddrivebye.usermanagement.domain.valueobject.AvailabilityStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * When a driver goes OFFLINE, drop their position from the Redis GEO set so
 * matching never sees them. When they come back AVAILABLE, no action is
 * needed: a position will be pushed by the next {@code updateDriverPosition}
 * call from the device. Per the bounded-context rules, geolocation reacts
 * to user-management events instead of calling its facade.
 */
@Component
public class DriverAvailabilityEventHandler {

    private final GeolocationCommandHandler commandHandler;

    public DriverAvailabilityEventHandler(GeolocationCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @EventListener
    public void on(DriverAvailabilityChangedEvent event) {
        if (event.newStatus() == AvailabilityStatus.OFFLINE) {
            commandHandler.handle(new RemoveDriverPositionCommand(event.userId().value()));
        }
    }
}
