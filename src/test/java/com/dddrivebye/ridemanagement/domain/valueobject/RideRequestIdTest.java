package com.dddrivebye.ridemanagement.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideRequestIdTest {

    @Test
    void shouldGenerateDistinctValues() {
        assertThat(RideRequestId.generate().value()).isNotEqualTo(RideRequestId.generate().value());
    }

    @Test
    void shouldWrapProvidedUuid() {
        UUID raw = UUID.randomUUID();

        assertThat(RideRequestId.of(raw).value()).isEqualTo(raw);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> RideRequestId.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RideRequestId value cannot be null");
    }

    @Test
    void shouldUseValueEquality() {
        UUID raw = UUID.randomUUID();

        assertThat(RideRequestId.of(raw)).isEqualTo(RideRequestId.of(raw));
        assertThat(RideRequestId.of(raw)).hasSameHashCodeAs(RideRequestId.of(raw));
    }
}
