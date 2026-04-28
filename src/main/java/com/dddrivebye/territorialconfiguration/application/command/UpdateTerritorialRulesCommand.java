package com.dddrivebye.territorialconfiguration.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateTerritorialRulesCommand(
        UUID territoryId,
        BigDecimal perKilometerRate,
        BigDecimal perMinuteRate,
        BigDecimal pickupFee,
        BigDecimal maxSurgeCoefficient,
        BigDecimal cancellationFee,
        boolean carpoolingGroupingEnabled,
        BigDecimal fixedFare,
        String currency
) {
}
