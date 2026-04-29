package com.dddrivebye.geolocation.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverIdTest {

    @Test
    void of_rejectsNull() {
        assertThatThrownBy(() -> DriverId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DriverId cannot be null");
    }

    @Test
    void equality_isValueBased() {
        UUID uuid = UUID.randomUUID();

        assertThat(DriverId.of(uuid)).isEqualTo(DriverId.of(uuid));
        assertThat(DriverId.of(uuid)).hasSameHashCodeAs(DriverId.of(uuid));
        assertThat(DriverId.of(UUID.randomUUID())).isNotEqualTo(DriverId.of(UUID.randomUUID()));
    }

    @Test
    void value_returnsUnderlyingUuid() {
        UUID uuid = UUID.randomUUID();
        assertThat(DriverId.of(uuid).value()).isEqualTo(uuid);
    }
}
