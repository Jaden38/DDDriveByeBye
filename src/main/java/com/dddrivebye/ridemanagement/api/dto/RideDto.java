package com.dddrivebye.ridemanagement.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RideDto(
        UUID id,
        UUID passengerId,
        UUID driverId,
        String state,
        double pickupLat,
        double pickupLon,
        double destLat,
        double destLon,
        BigDecimal priceAmount,
        String priceCurrency,
        int requestedSeats
) {
}
