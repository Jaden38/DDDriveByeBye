package com.dddrivebye.geolocation.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtaTest {

    @Test
    void ofMinutes_acceptsZero() {
        Eta eta = Eta.ofMinutes(0);

        assertThat(eta.toMinutes()).isZero();
        assertThat(eta.duration()).isEqualTo(Duration.ZERO);
    }

    @Test
    void ofMinutes_acceptsPositiveValue() {
        Eta eta = Eta.ofMinutes(7);

        assertThat(eta.toMinutes()).isEqualTo(7);
        assertThat(eta.duration()).isEqualTo(Duration.ofMinutes(7));
    }

    @Test
    void ofMinutes_rejectsNegative() {
        assertThatThrownBy(() -> Eta.ofMinutes(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ETA cannot be negative");
    }

    @Test
    void of_rejectsNullDuration() {
        assertThatThrownBy(() -> Eta.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void of_rejectsNegativeDuration() {
        assertThatThrownBy(() -> Eta.of(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_isValueBased() {
        assertThat(Eta.ofMinutes(3)).isEqualTo(Eta.ofMinutes(3));
        assertThat(Eta.ofMinutes(3)).hasSameHashCodeAs(Eta.ofMinutes(3));
        assertThat(Eta.ofMinutes(3)).isNotEqualTo(Eta.ofMinutes(4));
    }

    @Test
    void toString_isHumanReadable() {
        assertThat(Eta.ofMinutes(12)).hasToString("12 min");
    }
}
