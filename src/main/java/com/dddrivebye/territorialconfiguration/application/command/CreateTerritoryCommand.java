package com.dddrivebye.territorialconfiguration.application.command;

import java.math.BigDecimal;
import java.util.List;

public record CreateTerritoryCommand(
        String name,
        double centerLatitude,
        double centerLongitude,
        double radiusKm,
        BigDecimal perKilometerRate,
        BigDecimal perMinuteRate,
        BigDecimal pickupFee,
        BigDecimal maxSurgeCoefficient,
        BigDecimal cancellationFee,
        boolean carpoolingGroupingEnabled,
        BigDecimal fixedFare,
        String currency,
        List<RegulatoryConstraintInput> constraints
) {

    public record RegulatoryConstraintInput(String code, String description) {
    }
}
