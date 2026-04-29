package com.dddrivebye.ridemanagement.domain.exception;

import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideNotFoundExceptionTest {

    @Test
    void shouldFormatMessageFromRideId() {
        UUID raw = UUID.randomUUID();
        RideId id = RideId.of(raw);

        RideNotFoundException ex = new RideNotFoundException(id);

        assertThat(ex.getMessage()).contains(raw.toString());
    }
}
