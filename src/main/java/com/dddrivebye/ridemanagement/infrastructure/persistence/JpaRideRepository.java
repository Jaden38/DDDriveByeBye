package com.dddrivebye.ridemanagement.infrastructure.persistence;

import com.dddrivebye.ridemanagement.domain.entity.*;
import com.dddrivebye.ridemanagement.domain.repository.RideRepository;
import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaRideRepository implements RideRepository {

    private final SpringDataRideRepository springDataRepository;

    @Override
    public void save(Ride ride) {
        RideJpaEntity entity = new RideJpaEntity();
        entity.setId(ride.id().value());
        entity.setPassengerId(ride.passengerId().value());
        entity.setDriverId(ride.driverId() != null ? ride.driverId().value() : null);
        entity.setState(ride.state().getClass().getSimpleName());
        entity.setPickupLat(ride.pickupPoint().latitude());
        entity.setPickupLon(ride.pickupPoint().longitude());
        entity.setDestLat(ride.destination().latitude());
        entity.setDestLon(ride.destination().longitude());
        if (ride.price() != null) {
            entity.setPriceAmount(ride.price().amount());
            entity.setPriceCurrency(ride.price().currency().getCurrencyCode());
        }
        entity.setRequestedSeats(ride.requestedSeats());
        springDataRepository.save(entity);
    }

    @Override
    public Optional<Ride> findById(RideId id) {
        return springDataRepository.findById(id.value())
                .map(this::mapToDomain);
    }

    private Ride mapToDomain(RideJpaEntity entity) {
        RideState state = switch (entity.getState()) {
            case "RequestedState" -> new RequestedState();
            case "ProposedState" -> new ProposedState();
            case "AcceptedState" -> new AcceptedState();
            case "PickedUpState" -> new PickedUpState();
            case "InProgressState" -> new InProgressState();
            case "ArrivedState" -> new ArrivedState();
            case "FinalizedState" -> new FinalizedState();
            case "CancelledState" -> new CancelledState();
            case "IncidentState" -> new IncidentState();
            default -> throw new IllegalArgumentException("Unknown state: " + entity.getState());
        };

        return Ride.reconstitute(
                RideId.of(entity.getId()),
                UserId.of(entity.getPassengerId()),
                entity.getDriverId() != null ? UserId.of(entity.getDriverId()) : null,
                GeoCoordinates.of(entity.getPickupLat(), entity.getPickupLon()),
                GeoCoordinates.of(entity.getDestLat(), entity.getDestLon()),
                entity.getPriceAmount() != null ? Money.of(entity.getPriceAmount(), entity.getPriceCurrency()) : null,
                entity.getRequestedSeats(),
                state
        );
    }
}
