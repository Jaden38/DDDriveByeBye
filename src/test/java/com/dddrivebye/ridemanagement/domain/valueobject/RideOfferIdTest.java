package com.dddrivebye.ridemanagement.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideOfferIdTest {

    @Test
    void shouldGenerateDistinctValues() {
        assertThat(RideOfferId.generate().value()).isNotEqualTo(RideOfferId.generate().value());
    }

    @Test
    void shouldWrapProvidedUuid() {
        UUID raw = UUID.randomUUID();

        assertThat(RideOfferId.of(raw).value()).isEqualTo(raw);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> RideOfferId.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RideOfferId value cannot be null");
    }

    @Test
    void shouldUseValueEquality() {
        UUID raw = UUID.randomUUID();

        assertThat(RideOfferId.of(raw)).isEqualTo(RideOfferId.of(raw));
        assertThat(RideOfferId.of(raw)).hasSameHashCodeAs(RideOfferId.of(raw));
    }
}
