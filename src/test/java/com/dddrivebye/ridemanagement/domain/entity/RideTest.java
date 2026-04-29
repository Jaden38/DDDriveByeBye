package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.event.RideRequestedEvent;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideTest {

    private static final GeoCoordinates PICKUP = GeoCoordinates.of(48.8566, 2.3522);
    private static final GeoCoordinates DESTINATION = GeoCoordinates.of(48.8606, 2.3376);

    @Test
    void shouldEmitRideRequestedEventOnCreate() {
        UserId passenger = UserId.of(UUID.randomUUID());

        Ride ride = Ride.create(passenger, PICKUP, DESTINATION, 2);

        List<BaseDomainEvent> events = ride.pullDomainEvents();
        assertThat(events).hasSize(1);
        RideRequestedEvent event = (RideRequestedEvent) events.get(0);
        assertThat(event.rideId()).isEqualTo(ride.id());
        assertThat(event.passengerId()).isEqualTo(passenger);
        assertThat(event.pickupPoint()).isEqualTo(PICKUP);
        assertThat(event.destination()).isEqualTo(DESTINATION);
        assertThat(event.requestedSeats()).isEqualTo(2);
    }

    @Test
    void shouldStartInRequestedState() {
        Ride ride = Ride.create(UserId.of(UUID.randomUUID()), PICKUP, DESTINATION, 1);

        assertThat(ride.state()).isInstanceOf(RequestedState.class);
        assertThat(ride.driverId()).isNull();
        assertThat(ride.price()).isNull();
    }

    @Test
    void shouldClearEventsOnPull() {
        Ride ride = Ride.create(UserId.of(UUID.randomUUID()), PICKUP, DESTINATION, 1);
        ride.pullDomainEvents();

        assertThat(ride.pullDomainEvents()).isEmpty();
    }
}
