package com.dddrivebye.usermanagement.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record AddDriverProfileCommand(
        UUID userId,
        String driversLicenseNumber,
        LocalDate driversLicenseExpiry,
        String insuranceCompany,
        String insurancePolicyNumber,
        LocalDate insuranceExpiry,
        String vehicleMake,
        String vehicleModel,
        int vehicleYear,
        String vehicleLicensePlate
) {
}
