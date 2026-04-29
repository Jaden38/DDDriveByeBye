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
        
        publishEvents(ride);
        
        return ride.id().value();
    }

    @Transactional
    public void handle(ProposeDriverCommand command) {
        Ride ride = getRide(command.rideId());
        ride.propose(UserId.of(command.driverId()));
        rides.save(ride);
        publishEvents(ride);
    }

    @Transactional
    public void handle(AcceptRideCommand command) {
        Ride ride = getRide(command.rideId());
        ride.accept();
        rides.save(ride);
        publishEvents(ride);
    }

    @Transactional
    public void handle(PickUpPassengerCommand command) {
        Ride ride = getRide(command.rideId());
        ride.pickUp();
        rides.save(ride);
        publishEvents(ride);
    }

    @Transactional
    public void handle(StartRideCommand command) {
        Ride ride = getRide(command.rideId());
        ride.start();
        rides.save(ride);
        publishEvents(ride);
    }

    @Transactional
    public void handle(ArriveAtDestinationCommand command) {
        Ride ride = getRide(command.rideId());
        ride.arrive();
        rides.save(ride);
        publishEvents(ride);
    }

    @Transactional
    public void handle(FinalizeRideCommand command) {
        Ride ride = getRide(command.rideId());
        ride.finalizeRide();
        rides.save(ride);
        publishEvents(ride);
    }

    private Ride getRide(UUID id) {
        return rides.findById(RideId.of(id))
                .orElseThrow(() -> new com.dddrivebye.ridemanagement.domain.exception.RideNotFoundException(RideId.of(id)));
    }

    private void publishEvents(Ride ride) {
        ride.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
