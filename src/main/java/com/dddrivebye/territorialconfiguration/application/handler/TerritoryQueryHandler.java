package com.dddrivebye.territorialconfiguration.application.handler;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.territorialconfiguration.api.dto.TerritoryDto;
import com.dddrivebye.territorialconfiguration.application.query.GetRulesForTerritoryQuery;
import com.dddrivebye.territorialconfiguration.application.query.GetTerritoryForCoordinatesQuery;
import com.dddrivebye.territorialconfiguration.application.query.IsCoordinatesCoveredQuery;
import com.dddrivebye.territorialconfiguration.domain.entity.Territory;
import com.dddrivebye.territorialconfiguration.domain.repository.TerritoryRepository;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TerritoryQueryHandler {

    private final TerritoryRepository territories;

    public TerritoryQueryHandler(TerritoryRepository territories) {
        this.territories = territories;
    }

    @Transactional(readOnly = true)
    public Optional<TerritoryDto> handle(GetRulesForTerritoryQuery query) {
        return territories.findById(TerritoryId.of(query.territoryId())).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<TerritoryDto> handle(GetTerritoryForCoordinatesQuery query) {
        GeoCoordinates point = GeoCoordinates.of(query.latitude(), query.longitude());
        return territories.findActiveCovering(point).stream().findFirst().map(this::toDto);
    }

    @Transactional(readOnly = true)
    public boolean handle(IsCoordinatesCoveredQuery query) {
        GeoCoordinates point = GeoCoordinates.of(query.latitude(), query.longitude());
        return !territories.findActiveCovering(point).isEmpty();
    }

    private TerritoryDto toDto(Territory t) {
        TerritoryDto.TerritorialRuleDto ruleDto = new TerritoryDto.TerritorialRuleDto(
                t.rule().perKilometerRate().amount(),
                t.rule().perMinuteRate().amount(),
                t.rule().pickupFee().amount(),
                t.rule().maxSurgeCoefficient(),
                t.rule().cancellationFee().amount(),
                t.rule().carpoolingGroupingEnabled(),
                t.rule().fixedFare().map(m -> m.amount()).orElse(null),
                t.rule().perKilometerRate().currency().getCurrencyCode());
        List<TerritoryDto.RegulatoryConstraintDto> constraints = t.constraintsList().stream()
                .map(c -> new TerritoryDto.RegulatoryConstraintDto(c.code(), c.description()))
                .toList();
        return new TerritoryDto(
                t.id().value(),
                t.name(),
                t.zone().center().latitude(),
                t.zone().center().longitude(),
                t.zone().radiusKm(),
                t.isActive(),
                ruleDto,
                constraints);
    }
}
