package com.dddrivebye.ridemanagement.domain.repository;

import com.dddrivebye.ridemanagement.domain.entity.RideRequest;
import com.dddrivebye.ridemanagement.domain.valueobject.RideRequestId;

import java.util.Optional;

public interface RideRequestRepository {
    void save(RideRequest request);
    Optional<RideRequest> findById(RideRequestId id);
}
