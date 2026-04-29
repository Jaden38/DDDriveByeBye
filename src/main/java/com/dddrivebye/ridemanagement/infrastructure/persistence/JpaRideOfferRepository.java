package com.dddrivebye.ridemanagement.infrastructure.persistence;

import com.dddrivebye.ridemanagement.domain.entity.RideOffer;
import com.dddrivebye.ridemanagement.domain.repository.RideOfferRepository;
import com.dddrivebye.ridemanagement.domain.valueobject.RideOfferId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaRideOfferRepository implements RideOfferRepository {

    private final SpringDataRideOfferRepository springDataRepository;

    @Override
    public void save(RideOffer offer) {
        RideOfferJpaEntity entity = new RideOfferJpaEntity();
        entity.setId(offer.id().value());
        entity.setDriverId(offer.driverId().value());
        entity.setPickupLat(offer.pickupPoint().latitude());
        entity.setPickupLon(offer.pickupPoint().longitude());
        entity.setDestLat(offer.destination().latitude());
        entity.setDestLon(offer.destination().longitude());
        entity.setDepartureTime(offer.departureTime());
        entity.setTotalSeats(offer.totalSeats());
        entity.setAvailableSeats(offer.availableSeats());
        if (offer.pricePerSeat() != null) {
            entity.setPriceAmount(offer.pricePerSeat().amount());
            entity.setPriceCurrency(offer.pricePerSeat().currency().getCurrencyCode());
        }
        entity.setActive(offer.isActive());
        springDataRepository.save(entity);
    }

    @Override
    public Optional<RideOffer> findById(RideOfferId id) {
        return springDataRepository.findById(id.value())
                .map(entity -> RideOffer.reconstitute(
                        RideOfferId.of(entity.getId()),
                        UserId.of(entity.getDriverId()),
                        GeoCoordinates.of(entity.getPickupLat(), entity.getPickupLon()),
                        GeoCoordinates.of(entity.getDestLat(), entity.getDestLon()),
                        entity.getDepartureTime(),
                        entity.getTotalSeats(),
                        entity.getAvailableSeats(),
                        entity.getPriceAmount() != null ? Money.of(entity.getPriceAmount(), entity.getPriceCurrency()) : null,
                        entity.isActive()
                ));
    }
}
