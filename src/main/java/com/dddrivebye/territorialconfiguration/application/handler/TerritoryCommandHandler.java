package com.dddrivebye.territorialconfiguration.application.handler;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.territorialconfiguration.application.command.CreateTerritoryCommand;
import com.dddrivebye.territorialconfiguration.application.command.DeactivateTerritoryCommand;
import com.dddrivebye.territorialconfiguration.application.command.UpdateTerritorialRulesCommand;
import com.dddrivebye.territorialconfiguration.domain.entity.Territory;
import com.dddrivebye.territorialconfiguration.domain.exception.TerritoryNotFoundException;
import com.dddrivebye.territorialconfiguration.domain.repository.TerritoryRepository;
import com.dddrivebye.territorialconfiguration.domain.valueobject.GeographicZone;
import com.dddrivebye.territorialconfiguration.domain.valueobject.RegulatoryConstraint;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritorialRule;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TerritoryCommandHandler {

    private final TerritoryRepository territories;

    public TerritoryCommandHandler(TerritoryRepository territories) {
        this.territories = territories;
    }

    @Transactional
    public UUID handle(CreateTerritoryCommand command) {
        GeoCoordinates center = GeoCoordinates.of(command.centerLatitude(), command.centerLongitude());
        GeographicZone zone = GeographicZone.of(center, command.radiusKm());
        TerritorialRule rule = buildRule(
                command.perKilometerRate(), command.perMinuteRate(),
                command.pickupFee(), command.maxSurgeCoefficient(),
                command.cancellationFee(), command.carpoolingGroupingEnabled(),
                command.fixedFare(), command.currency());
        Territory territory = Territory.create(command.name(), zone, rule);
        if (command.constraints() != null) {
            command.constraints().forEach(c ->
                    territory.addConstraint(RegulatoryConstraint.of(c.code(), c.description())));
        }
        territories.save(territory);
        return territory.id().value();
    }

    @Transactional
    public void handle(UpdateTerritorialRulesCommand command) {
        Territory territory = load(command.territoryId());
        TerritorialRule rule = buildRule(
                command.perKilometerRate(), command.perMinuteRate(),
                command.pickupFee(), command.maxSurgeCoefficient(),
                command.cancellationFee(), command.carpoolingGroupingEnabled(),
                command.fixedFare(), command.currency());
        territory.updateRule(rule);
        territories.save(territory);
    }

    @Transactional
    public void handle(DeactivateTerritoryCommand command) {
        Territory territory = load(command.territoryId());
        territory.deactivate();
        territories.save(territory);
    }

    private Territory load(UUID id) {
        TerritoryId territoryId = TerritoryId.of(id);
        return territories.findById(territoryId)
                .orElseThrow(() -> new TerritoryNotFoundException("Territory not found: " + id));
    }

    private TerritorialRule buildRule(BigDecimal perKm, BigDecimal perMin, BigDecimal pickup,
                                      BigDecimal surge, BigDecimal cancellation,
                                      boolean carpooling, BigDecimal fixedFare, String currency) {
        return TerritorialRule.of(
                Money.of(perKm, currency),
                Money.of(perMin, currency),
                Money.of(pickup, currency),
                surge,
                Money.of(cancellation, currency),
                carpooling,
                fixedFare == null ? null : Money.of(fixedFare, currency));
    }
}
