package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.valueobject.RideRequestId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideRequestTest {

    private static final GeoCoordinates PICKUP = GeoCoordinates.of(48.8566, 2.3522);
    private static final GeoCoordinates DESTINATION = GeoCoordinates.of(48.8606, 2.3376);
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 5, 1, 9, 0);

    @Test
    void shouldCreateUnfulfilledRequest() {
        UserId passenger = UserId.of(UUID.randomUUID());

        RideRequest request = RideRequest.create(passenger, PICKUP, DESTINATION, REQUESTED_AT, 2);

        assertThat(request.id()).isNotNull();
        assertThat(request.passengerId()).isEqualTo(passenger);
        assertThat(request.pickupPoint()).isEqualTo(PICKUP);
        assertThat(request.destination()).isEqualTo(DESTINATION);
        assertThat(request.requestedTime()).isEqualTo(REQUESTED_AT);
        assertThat(request.requestedSeats()).isEqualTo(2);
        assertThat(request.isFulfilled()).isFalse();
    }

    @Test
    void shouldRehydrateFromPersistedFields() {
        RideRequestId id = RideRequestId.generate();
        UserId passenger = UserId.of(UUID.randomUUID());

        RideRequest request = RideRequest.reconstitute(
                id, passenger, PICKUP, DESTINATION, REQUESTED_AT, 1, true);

        assertThat(request.id()).isEqualTo(id);
        assertThat(request.passengerId()).isEqualTo(passenger);
        assertThat(request.requestedSeats()).isEqualTo(1);
        assertThat(request.isFulfilled()).isTrue();
    }

    @Test
    void shouldFlagAsFulfilled() {
        RideRequest request = RideRequest.create(
                UserId.of(UUID.randomUUID()), PICKUP, DESTINATION, REQUESTED_AT, 1);

        request.fulfill();

        assertThat(request.isFulfilled()).isTrue();
    }
}
