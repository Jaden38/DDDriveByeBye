package com.dddrivebye.usermanagement.application.command;

import java.time.LocalDate;

public record RegisterProfessionalAccountCommand(
        String fullName,
        String email,
        String phoneNumber,
        String vtcLicenseNumber,
        LocalDate vtcLicenseExpiry,
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
