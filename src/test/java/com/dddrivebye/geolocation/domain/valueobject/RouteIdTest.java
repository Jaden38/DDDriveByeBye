package com.dddrivebye.geolocation.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteIdTest {

    @Test
    void of_rejectsNull() {
        assertThatThrownBy(() -> RouteId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RouteId cannot be null");
    }

    @Test
    void generate_producesDistinctIds() {
        assertThat(RouteId.generate()).isNotEqualTo(RouteId.generate());
    }

    @Test
    void equality_isValueBased() {
        UUID uuid = UUID.randomUUID();

        assertThat(RouteId.of(uuid)).isEqualTo(RouteId.of(uuid));
        assertThat(RouteId.of(uuid)).hasSameHashCodeAs(RouteId.of(uuid));
    }
}
