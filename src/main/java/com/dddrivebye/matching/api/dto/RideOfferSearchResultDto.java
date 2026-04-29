package com.dddrivebye.matching.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RideOfferSearchResultDto(
        UUID rideOfferId,
        UUID driverId,
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude,
        LocalDateTime departureAt,
        int seatsAvailable,
        long minutesFromPreferredTime
) {
}
