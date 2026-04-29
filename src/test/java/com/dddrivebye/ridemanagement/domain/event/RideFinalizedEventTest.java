package com.dddrivebye.ridemanagement.domain.event;

import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RideFinalizedEventTest {

    @Test
    void shouldCarryRideIdAndStandardEventMetadata() {
        RideId rideId = RideId.generate();

        RideFinalizedEvent event = new RideFinalizedEvent(rideId);

        assertThat(event.rideId()).isEqualTo(rideId);
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }
}
