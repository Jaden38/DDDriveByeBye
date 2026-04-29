package com.dddrivebye.matching.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RideKindTest {

    @Test
    void immediateAllowsProfessionalDrivers() {
        assertThat(RideKind.IMMEDIATE.allowsProfessionalDrivers()).isTrue();
    }

    @Test
    void scheduledExcludesProfessionalDrivers() {
        assertThat(RideKind.SCHEDULED.allowsProfessionalDrivers()).isFalse();
    }
}
