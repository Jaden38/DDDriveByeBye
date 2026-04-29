package com.dddrivebye.usermanagement.api.dto;

import java.util.UUID;

public record AvailableDriverDto(
        UUID driverId,
        String accountType,
        double latitude,
        double longitude
) {
}
