package com.dddrivebye.usermanagement.domain.entity;

import com.dddrivebye.usermanagement.domain.valueobject.AvailabilityStatus;

public class Availability {

    private AvailabilityStatus status;

    private Availability(AvailabilityStatus status) {
        this.status = status;
    }

    public static Availability offline() {
        return new Availability(AvailabilityStatus.OFFLINE);
    }

    public static Availability reconstitute(AvailabilityStatus status) {
        return new Availability(status);
    }

    public AvailabilityStatus status() {
        return status;
    }

    public AvailabilityStatus activate() {
        AvailabilityStatus previous = this.status;
        this.status = AvailabilityStatus.AVAILABLE;
        return previous;
    }

    public AvailabilityStatus deactivate() {
        AvailabilityStatus previous = this.status;
        this.status = AvailabilityStatus.OFFLINE;
        return previous;
    }

    public boolean isAvailable() {
        return status == AvailabilityStatus.AVAILABLE;
    }
}
