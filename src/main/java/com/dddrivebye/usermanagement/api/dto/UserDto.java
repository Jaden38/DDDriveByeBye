package com.dddrivebye.usermanagement.api.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        String accountType,
        String status
) {
}
