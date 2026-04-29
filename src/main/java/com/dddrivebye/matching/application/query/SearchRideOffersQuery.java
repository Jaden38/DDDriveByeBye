package com.dddrivebye.matching.application.query;

import com.dddrivebye.matching.domain.valueobject.RideOption;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record SearchRideOffersQuery(
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude,
        LocalDate date,
        LocalTime preferredTime,
        Set<RideOption> requiredOptions
) {
}
