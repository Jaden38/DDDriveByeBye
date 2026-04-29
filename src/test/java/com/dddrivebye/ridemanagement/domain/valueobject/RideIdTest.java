package com.dddrivebye.ridemanagement.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideIdTest {

    @Test
    void shouldGenerateDistinctValues() {
        assertThat(RideId.generate().value()).isNotEqualTo(RideId.generate().value());
    }

    @Test
    void shouldWrapProvidedUuid() {
        UUID raw = UUID.randomUUID();

        assertThat(RideId.of(raw).value()).isEqualTo(raw);
    }

    @Test
    void shouldParseUuidFromString() {
        UUID raw = UUID.randomUUID();

        assertThat(RideId.of(raw.toString()).value()).isEqualTo(raw);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> RideId.of((UUID) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RideId value cannot be null");
    }

    @Test
    void shouldUseValueEqualityAndStableHash() {
        UUID raw = UUID.randomUUID();
        RideId a = RideId.of(raw);
        RideId b = RideId.of(raw);

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(RideId.generate());
    }

    @Test
    void recordToStringExposesValue() {
        RideId id = RideId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(id.toString()).contains("00000000-0000-0000-0000-000000000001");
    }
}
