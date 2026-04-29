package com.dddrivebye.ridemanagement.domain.repository;

import com.dddrivebye.ridemanagement.domain.entity.RideOffer;
import com.dddrivebye.ridemanagement.domain.valueobject.RideOfferId;

import java.util.Optional;

public interface RideOfferRepository {
    void save(RideOffer offer);
    Optional<RideOffer> findById(RideOfferId id);
}
