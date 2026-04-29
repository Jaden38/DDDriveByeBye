package com.dddrivebye.territorialconfiguration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTerritoryRepository extends JpaRepository<TerritoryJpaEntity, UUID> {

    Optional<TerritoryJpaEntity> findByName(String name);

    @Query("select t from TerritoryJpaEntity t where t.active = true")
    List<TerritoryJpaEntity> findAllActive();
}
