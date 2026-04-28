package com.dddrivebye.territorialconfiguration.infrastructure.persistence;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.territorialconfiguration.domain.entity.Territory;
import com.dddrivebye.territorialconfiguration.domain.repository.TerritoryRepository;
import com.dddrivebye.territorialconfiguration.domain.valueobject.GeographicZone;
import com.dddrivebye.territorialconfiguration.domain.valueobject.RegulatoryConstraint;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritorialRule;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritoryId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaTerritoryRepository implements TerritoryRepository {

    private final SpringDataTerritoryRepository delegate;

    public JpaTerritoryRepository(SpringDataTerritoryRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Territory territory) {
        TerritoryJpaEntity entity = delegate.findById(territory.id().value())
                .orElseGet(TerritoryJpaEntity::new);
        toJpa(territory, entity);
        delegate.save(entity);
    }

    @Override
    public Optional<Territory> findById(TerritoryId id) {
        return delegate.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Territory> findByName(String name) {
        return delegate.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Territory> findActiveCovering(GeoCoordinates point) {
        return delegate.findAllActive().stream()
                .map(this::toDomain)
                .filter(t -> t.covers(point))
                .collect(Collectors.toList());
    }

    private void toJpa(Territory territory, TerritoryJpaEntity entity) {
        entity.setId(territory.id().value());
        entity.setName(territory.name());
        entity.setCenterLat(territory.zone().center().latitude());
        entity.setCenterLon(territory.zone().center().longitude());
        entity.setRadiusKm(territory.zone().radiusKm());
        entity.setActive(territory.isActive());

        TerritorialRule rule = territory.rule();
        entity.setCurrency(rule.perKilometerRate().currency().getCurrencyCode());
        entity.setPerKmRate(rule.perKilometerRate().amount());
        entity.setPerMinRate(rule.perMinuteRate().amount());
        entity.setPickupFee(rule.pickupFee().amount());
        entity.setMaxSurgeCoefficient(rule.maxSurgeCoefficient());
        entity.setCancellationFee(rule.cancellationFee().amount());
        entity.setCarpoolingGroupingEnabled(rule.carpoolingGroupingEnabled());
        entity.setFixedFare(rule.fixedFare().map(Money::amount).orElse(null));

        entity.getConstraints().clear();
        for (RegulatoryConstraint c : territory.constraintsList()) {
            TerritoryConstraintJpaEntity ce = new TerritoryConstraintJpaEntity();
            ce.setId(UUID.randomUUID());
            ce.setTerritory(entity);
            ce.setCode(c.code());
            ce.setDescription(c.description());
            entity.getConstraints().add(ce);
        }
    }

    private Territory toDomain(TerritoryJpaEntity entity) {
        GeoCoordinates center = GeoCoordinates.of(entity.getCenterLat(), entity.getCenterLon());
        GeographicZone zone = GeographicZone.of(center, entity.getRadiusKm());
        String currency = entity.getCurrency();
        TerritorialRule rule = TerritorialRule.of(
                Money.of(entity.getPerKmRate(), currency),
                Money.of(entity.getPerMinRate(), currency),
                Money.of(entity.getPickupFee(), currency),
                entity.getMaxSurgeCoefficient(),
                Money.of(entity.getCancellationFee(), currency),
                entity.isCarpoolingGroupingEnabled(),
                entity.getFixedFare() == null ? null : Money.of(entity.getFixedFare(), currency));
        java.util.Set<RegulatoryConstraint> constraints = entity.getConstraints().stream()
                .map(c -> RegulatoryConstraint.of(c.getCode(), c.getDescription()))
                .collect(Collectors.toSet());
        return Territory.reconstitute(
                TerritoryId.of(entity.getId()),
                entity.getName(),
                zone,
                rule,
                constraints,
                entity.isActive());
    }
}
