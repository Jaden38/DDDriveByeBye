package com.dddrivebye.territorialconfiguration.domain.repository;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.territorialconfiguration.domain.entity.Territory;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritoryId;

import java.util.List;
import java.util.Optional;

public interface TerritoryRepository {

    void save(Territory territory);

    Optional<Territory> findById(TerritoryId id);

    Optional<Territory> findByName(String name);

    List<Territory> findActiveCovering(GeoCoordinates point);
}
