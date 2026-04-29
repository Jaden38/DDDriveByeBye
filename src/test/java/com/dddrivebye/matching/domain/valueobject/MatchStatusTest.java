package com.dddrivebye.matching.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchStatusTest {

    @Test
    void shouldClassifyTerminalStates() {
        assertThat(MatchStatus.ACCEPTED.isTerminal()).isTrue();
        assertThat(MatchStatus.UNMATCHED.isTerminal()).isTrue();
        assertThat(MatchStatus.CANCELLED.isTerminal()).isTrue();
    }

    @Test
    void shouldClassifyNonTerminalStates() {
        assertThat(MatchStatus.SEARCHING.isTerminal()).isFalse();
        assertThat(MatchStatus.PROPOSED.isTerminal()).isFalse();
    }
}
