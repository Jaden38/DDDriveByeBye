package com.dddrivebye.ridemanagement.application.handler;

import com.dddrivebye.ridemanagement.application.command.RequestRideCommand;
import com.dddrivebye.ridemanagement.domain.entity.Ride;
import com.dddrivebye.ridemanagement.domain.repository.RideRepository;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideCommandHandler {

    private final RideRepository rides;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public UUID handle(RequestRideCommand command) {
        Ride ride = Ride.create(
                UserId.of(command.passengerId()),
                GeoCoordinates.of(command.pickupLat(), command.pickupLon()),
                GeoCoordinates.of(command.destLat(), command.destLon()),
                command.requestedSeats()
        );
        
        rides.save(ride);
        
        ride.pullDomainEvents().forEach(eventPublisher::publish);
        
        return ride.id().value();
    }
}
