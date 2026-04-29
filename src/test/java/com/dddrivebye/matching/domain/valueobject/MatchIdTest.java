package com.dddrivebye.matching.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchIdTest {

    @Test
    void shouldGenerateDistinctValues() {
        MatchId a = MatchId.generate();
        MatchId b = MatchId.generate();

        assertThat(a.value()).isNotEqualTo(b.value());
    }

    @Test
    void shouldWrapProvidedUuid() {
        UUID raw = UUID.randomUUID();

        assertThat(MatchId.of(raw).value()).isEqualTo(raw);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> MatchId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldUseValueEquality() {
        UUID raw = UUID.randomUUID();

        assertThat(MatchId.of(raw)).isEqualTo(MatchId.of(raw));
        assertThat(MatchId.of(raw)).hasSameHashCodeAs(MatchId.of(raw));
        assertThat(MatchId.of(raw).toString()).isEqualTo(raw.toString());
    }
}
