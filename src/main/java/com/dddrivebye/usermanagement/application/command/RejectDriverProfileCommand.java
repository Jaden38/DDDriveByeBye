package com.dddrivebye.usermanagement.application.command;

import java.util.UUID;

public record RejectDriverProfileCommand(UUID userId, String reason) {
}
