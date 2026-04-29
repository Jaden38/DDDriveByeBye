package com.dddrivebye.matching.application.command;

import com.dddrivebye.matching.domain.valueobject.RideOption;

import java.util.Set;
import java.util.UUID;

public record RunImmediateMatchingCommand(
        UUID rideId,
        double pickupLatitude,
        double pickupLongitude,
        double searchRadiusKm,
        Set<RideOption> requiredOptions,
        boolean passengerHasPet,
        UUID territoryId,
        boolean territoryRequiresVtcLicense
) {
}
