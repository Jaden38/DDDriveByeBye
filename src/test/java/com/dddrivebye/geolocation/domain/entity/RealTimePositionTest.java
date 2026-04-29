package com.dddrivebye.geolocation.domain.entity;

import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RealTimePositionTest {

    private final DriverId driverId = DriverId.of(UUID.randomUUID());
    private final GeoCoordinates paris = GeoCoordinates.of(48.8566, 2.3522);

    @Test
    void capture_setsCapturedAtToNow() {
        Instant before = Instant.now();
        RealTimePosition position = RealTimePosition.capture(driverId, paris);
        Instant after = Instant.now();

        assertThat(position.driverId()).isEqualTo(driverId);
        assertThat(position.coordinates()).isEqualTo(paris);
        assertThat(position.capturedAt()).isBetween(before, after);
    }

    @Test
    void reconstitute_preservesCapturedAt() {
        Instant capturedAt = Instant.parse("2026-01-15T08:30:00Z");

        RealTimePosition position = RealTimePosition.reconstitute(driverId, paris, capturedAt);

        assertThat(position.capturedAt()).isEqualTo(capturedAt);
    }

    @Test
    void equality_requiresAllFieldsToMatch() {
        Instant t = Instant.parse("2026-01-15T08:30:00Z");
        RealTimePosition a = RealTimePosition.reconstitute(driverId, paris, t);
        RealTimePosition b = RealTimePosition.reconstitute(driverId, paris, t);
        RealTimePosition different = RealTimePosition.reconstitute(
                driverId, paris, t.plusSeconds(1));

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(different);
    }
}
