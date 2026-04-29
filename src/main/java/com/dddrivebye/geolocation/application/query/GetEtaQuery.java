package com.dddrivebye.geolocation.application.query;

public record GetEtaQuery(
        double fromLatitude,
        double fromLongitude,
        double toLatitude,
        double toLongitude
) {
}
