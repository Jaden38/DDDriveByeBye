package com.dddrivebye.usermanagement.application.command;

public record RegisterIndividualAccountCommand(
        String fullName,
        String email,
        String phoneNumber
) {
}
