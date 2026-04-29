package com.dddrivebye.geolocation.domain.repository;

import com.dddrivebye.geolocation.domain.entity.Route;
import com.dddrivebye.geolocation.domain.valueobject.RouteId;

import java.util.Optional;

public interface RouteRepository {

    void save(Route route);

    Optional<Route> findById(RouteId id);
}
