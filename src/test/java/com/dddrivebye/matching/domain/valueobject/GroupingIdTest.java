package com.dddrivebye.matching.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupingIdTest {

    @Test
    void shouldGenerateDistinctValues() {
        assertThat(GroupingId.generate().value()).isNotEqualTo(GroupingId.generate().value());
    }

    @Test
    void shouldWrapProvidedUuid() {
        UUID raw = UUID.randomUUID();

        assertThat(GroupingId.of(raw).value()).isEqualTo(raw);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> GroupingId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldUseValueEquality() {
        UUID raw = UUID.randomUUID();

        assertThat(GroupingId.of(raw)).isEqualTo(GroupingId.of(raw));
        assertThat(GroupingId.of(raw)).hasSameHashCodeAs(GroupingId.of(raw));
    }
}
