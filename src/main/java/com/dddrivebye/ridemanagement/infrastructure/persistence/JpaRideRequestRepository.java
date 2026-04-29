package com.dddrivebye.ridemanagement.infrastructure.persistence;

import com.dddrivebye.ridemanagement.domain.entity.RideRequest;
import com.dddrivebye.ridemanagement.domain.repository.RideRequestRepository;
import com.dddrivebye.ridemanagement.domain.valueobject.RideRequestId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaRideRequestRepository implements RideRequestRepository {

    private final SpringDataRideRequestRepository springDataRepository;

    @Override
    public void save(RideRequest request) {
        RideRequestJpaEntity entity = new RideRequestJpaEntity();
        entity.setId(request.id().value());
        entity.setPassengerId(request.passengerId().value());
        entity.setPickupLat(request.pickupPoint().latitude());
        entity.setPickupLon(request.pickupPoint().longitude());
        entity.setDestLat(request.destination().latitude());
        entity.setDestLon(request.destination().longitude());
        entity.setRequestedTime(request.requestedTime());
        entity.setRequestedSeats(request.requestedSeats());
        entity.setFulfilled(request.isFulfilled());
        springDataRepository.save(entity);
    }

    @Override
    public Optional<RideRequest> findById(RideRequestId id) {
        return springDataRepository.findById(id.value())
                .map(entity -> RideRequest.reconstitute(
                        RideRequestId.of(entity.getId()),
                        UserId.of(entity.getPassengerId()),
                        GeoCoordinates.of(entity.getPickupLat(), entity.getPickupLon()),
                        GeoCoordinates.of(entity.getDestLat(), entity.getDestLon()),
                        entity.getRequestedTime(),
                        entity.getRequestedSeats(),
                        entity.isFulfilled()
                ));
    }
}
