package com.dddrivebye.matching.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverCandidateTest {

    @Test
    void shouldCreateCandidateWhenInputsValid() {
        UUID id = UUID.randomUUID();

        DriverCandidate candidate = DriverCandidate.of(id, "INDIVIDUAL", 1.5, 4.6, true);

        assertThat(candidate.driverId()).isEqualTo(id);
        assertThat(candidate.accountType()).isEqualTo("INDIVIDUAL");
        assertThat(candidate.distanceKm()).isEqualTo(1.5);
        assertThat(candidate.reputationScore()).isEqualTo(4.6);
        assertThat(candidate.withinActivityZone()).isTrue();
    }

    @Test
    void shouldRejectNullIdentifiers() {
        assertThatThrownBy(() -> DriverCandidate.of(null, "INDIVIDUAL", 1.0, 4.5, false))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DriverCandidate.of(UUID.randomUUID(), null, 1.0, 4.5, false))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNegativeDistance() {
        assertThatThrownBy(() -> DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", -0.1, 4.5, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseValueEquality() {
        UUID id = UUID.randomUUID();
        DriverCandidate a = DriverCandidate.of(id, "INDIVIDUAL", 2.0, 4.5, true);
        DriverCandidate b = DriverCandidate.of(id, "INDIVIDUAL", 2.0, 4.5, true);
        DriverCandidate other = DriverCandidate.of(id, "INDIVIDUAL", 2.0, 4.6, true);

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(other);
    }
}
