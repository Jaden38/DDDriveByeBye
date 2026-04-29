package com.dddrivebye.matching.domain.repository;

import com.dddrivebye.matching.domain.entity.Grouping;
import com.dddrivebye.matching.domain.valueobject.GroupingId;

import java.util.Optional;
import java.util.UUID;

public interface GroupingRepository {

    void save(Grouping grouping);

    Optional<Grouping> findById(GroupingId id);

    Optional<Grouping> findActiveByRideRequestId(UUID rideRequestId);
}
