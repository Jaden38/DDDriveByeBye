package com.dddrivebye.matching.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProposalWindowTest {

    @Test
    void shouldOpenWindowEndingAtNowPlusDuration() {
        UUID driver = UUID.randomUUID();
        Instant now = Instant.parse("2026-04-29T10:00:00Z");

        ProposalWindow window = ProposalWindow.opening(driver, now, Duration.ofSeconds(30));

        assertThat(window.driverId()).isEqualTo(driver);
        assertThat(window.expiresAt()).isEqualTo(Instant.parse("2026-04-29T10:00:30Z"));
    }

    @Test
    void shouldRejectNullArguments() {
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        assertThatThrownBy(() -> ProposalWindow.opening(null, now, Duration.ofSeconds(30)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ProposalWindow.opening(UUID.randomUUID(), null, Duration.ofSeconds(30)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ProposalWindow.opening(UUID.randomUUID(), now, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNonPositiveWindow() {
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        assertThatThrownBy(() -> ProposalWindow.opening(UUID.randomUUID(), now, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProposalWindow.opening(UUID.randomUUID(), now, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReportExpiryAtAndAfterDeadline() {
        Instant now = Instant.parse("2026-04-29T10:00:00Z");
        ProposalWindow window = ProposalWindow.opening(UUID.randomUUID(), now, Duration.ofSeconds(30));

        assertThat(window.hasExpiredAt(Instant.parse("2026-04-29T10:00:29Z"))).isFalse();
        assertThat(window.hasExpiredAt(Instant.parse("2026-04-29T10:00:30Z"))).isTrue();
        assertThat(window.hasExpiredAt(Instant.parse("2026-04-29T10:00:31Z"))).isTrue();
    }

    @Test
    void shouldRehydrateFromPersistedFields() {
        UUID driver = UUID.randomUUID();
        Instant expiry = Instant.parse("2026-04-29T10:00:30Z");

        ProposalWindow window = ProposalWindow.rehydrate(driver, expiry);

        assertThat(window.driverId()).isEqualTo(driver);
        assertThat(window.expiresAt()).isEqualTo(expiry);
    }

    @Test
    void shouldHonourValueEqualityAndHashing() {
        UUID driver = UUID.randomUUID();
        Instant expiry = Instant.parse("2026-04-29T10:00:30Z");
        ProposalWindow a = ProposalWindow.rehydrate(driver, expiry);
        ProposalWindow b = ProposalWindow.rehydrate(driver, expiry);
        ProposalWindow c = ProposalWindow.rehydrate(UUID.randomUUID(), expiry);

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }
}
