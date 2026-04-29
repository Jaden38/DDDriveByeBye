package com.dddrivebye.territorialconfiguration.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TerritoryDto(
        UUID id,
        String name,
        double centerLatitude,
        double centerLongitude,
        double radiusKm,
        boolean active,
        TerritorialRuleDto rule,
        List<RegulatoryConstraintDto> constraints
) {

    public record TerritorialRuleDto(
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

    public record RegulatoryConstraintDto(String code, String description) {
    }
}
