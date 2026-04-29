package com.dddrivebye.ridemanagement.application.handler;

import com.dddrivebye.ridemanagement.api.dto.RideDto;
import com.dddrivebye.ridemanagement.application.query.GetRideByIdQuery;
import com.dddrivebye.ridemanagement.domain.repository.RideRepository;
import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideQueryHandler {

    private final RideRepository rides;

    @Transactional(readOnly = true)
    public Optional<RideDto> handle(GetRideByIdQuery query) {
        return rides.findById(RideId.of(query.rideId()))
                .map(ride -> new RideDto(
                        ride.id().value(),
                        ride.passengerId().value(),
                        ride.driverId() != null ? ride.driverId().value() : null,
                        ride.state().getClass().getSimpleName(),
                        ride.pickupPoint().latitude(),
                        ride.pickupPoint().longitude(),
                        ride.destination().latitude(),
                        ride.destination().longitude(),
                        ride.price() != null ? ride.price().amount() : null,
                        ride.price() != null ? ride.price().currency().getCurrencyCode() : null,
                        ride.requestedSeats()
                ));
    }
}
