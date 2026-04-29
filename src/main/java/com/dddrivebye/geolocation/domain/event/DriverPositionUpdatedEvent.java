package com.dddrivebye.geolocation.domain.event;

import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

public final class DriverPositionUpdatedEvent extends BaseDomainEvent {

    private final DriverId driverId;
    private final GeoCoordinates coordinates;

    public DriverPositionUpdatedEvent(DriverId driverId, GeoCoordinates coordinates) {
        this.driverId = driverId;
        this.coordinates = coordinates;
    }

    public DriverId driverId() {
        return driverId;
    }

    public GeoCoordinates coordinates() {
        return coordinates;
    }
}
