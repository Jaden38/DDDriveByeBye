package com.dddrivebye.matching.domain.repository;

import com.dddrivebye.matching.domain.entity.Match;
import com.dddrivebye.matching.domain.valueobject.MatchId;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {

    void save(Match match);

    Optional<Match> findById(MatchId id);

    Optional<Match> findByRideId(UUID rideId);
}
