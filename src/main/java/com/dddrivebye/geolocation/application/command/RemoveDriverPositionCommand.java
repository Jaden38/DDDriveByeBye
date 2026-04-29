package com.dddrivebye.geolocation.application.command;

import java.util.UUID;

public record RemoveDriverPositionCommand(UUID driverId) {
}
