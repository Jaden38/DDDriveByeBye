package com.dddrivebye.ridemanagement.domain.repository;

import com.dddrivebye.ridemanagement.domain.entity.Ride;
import com.dddrivebye.ridemanagement.domain.valueobject.RideId;

import java.util.Optional;

public interface RideRepository {
    void save(Ride ride);
    Optional<Ride> findById(RideId id);
}
