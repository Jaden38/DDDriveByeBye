package com.dddrivebye.matching.domain.service;

import com.dddrivebye.matching.domain.valueobject.DriverCandidate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DriverRankingTest {

    @Test
    void shouldPrioritizeDriversWithinActivityZone() {
        DriverCandidate inside = DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", 2.0, 4.5, true);
        DriverCandidate outside = DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", 1.0, 4.5, false);

        List<DriverCandidate> ranked = DriverRanking.rank(List.of(outside, inside));

        assertThat(ranked).containsExactly(inside, outside);
    }

    @Test
    void shouldOrderByDistanceWhenZoneEqual() {
        DriverCandidate near = DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", 0.9, 4.6, false);
        DriverCandidate far = DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", 1.2, 4.8, false);

        List<DriverCandidate> ranked = DriverRanking.rank(List.of(far, near));

        assertThat(ranked).containsExactly(near, far);
    }

    @Test
    void shouldUseReputationAsTiebreakerWhenDistanceEqual() {
        DriverCandidate higher = DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", 1.5, 4.9, false);
        DriverCandidate lower = DriverCandidate.of(UUID.randomUUID(), "INDIVIDUAL", 1.5, 4.0, false);

        List<DriverCandidate> ranked = DriverRanking.rank(List.of(lower, higher));

        assertThat(ranked).containsExactly(higher, lower);
    }

    @Test
    void shouldReturnEmptyListAsIs() {
        assertThat(DriverRanking.rank(List.of())).isEmpty();
    }

    @Test
    void shouldComposeAllThreeCriteria() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        UUID d = UUID.randomUUID();

        // Within-zone drivers ranked by distance first; out-of-zone drivers
        // come after, again ordered by distance.
        DriverCandidate marc = DriverCandidate.of(a, "INDIVIDUAL", 1.0, 4.7, true);
        DriverCandidate alice = DriverCandidate.of(b, "INDIVIDUAL", 2.0, 4.6, true);
        DriverCandidate paul = DriverCandidate.of(c, "INDIVIDUAL", 1.5, 4.5, false);
        DriverCandidate jean = DriverCandidate.of(d, "INDIVIDUAL", 3.0, 4.5, false);

        List<DriverCandidate> ranked = DriverRanking.rank(List.of(jean, alice, marc, paul));

        assertThat(ranked).containsExactly(marc, alice, paul, jean);
    }
}
