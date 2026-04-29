package com.dddrivebye.geolocation.domain.entity;

import com.dddrivebye.geolocation.domain.valueobject.RouteId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteTest {

    private final GeoCoordinates origin = GeoCoordinates.of(48.8566, 2.3522);
    private final GeoCoordinates destination = GeoCoordinates.of(45.7640, 4.8357);

    @Test
    void create_assignsGeneratedIdAndCalculatedAt() {
        Instant before = Instant.now();
        Route route = Route.create(origin, destination, 463.5, Duration.ofMinutes(240));
        Instant after = Instant.now();

        assertThat(route.id()).isNotNull();
        assertThat(route.origin()).isEqualTo(origin);
        assertThat(route.destination()).isEqualTo(destination);
        assertThat(route.distanceKm()).isEqualTo(463.5);
        assertThat(route.duration()).isEqualTo(Duration.ofMinutes(240));
        assertThat(route.calculatedAt()).isBetween(before, after);
    }

    @Test
    void create_rejectsNegativeDistance() {
        assertThatThrownBy(() -> Route.create(origin, destination, -1.0, Duration.ofMinutes(10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance cannot be negative");
    }

    @Test
    void create_rejectsNullDuration() {
        assertThatThrownBy(() -> Route.create(origin, destination, 10.0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be non-negative");
    }

    @Test
    void create_rejectsNegativeDuration() {
        assertThatThrownBy(() -> Route.create(origin, destination, 10.0, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_acceptsZeroDistanceAndZeroDuration() {
        Route route = Route.create(origin, origin, 0.0, Duration.ZERO);

        assertThat(route.distanceKm()).isZero();
        assertThat(route.duration()).isEqualTo(Duration.ZERO);
    }

    @Test
    void reconstitute_preservesProvidedFields() {
        RouteId id = RouteId.generate();
        Instant calculatedAt = Instant.parse("2026-01-15T08:30:00Z");

        Route route = Route.reconstitute(id, origin, destination, 5.5, Duration.ofMinutes(15), calculatedAt);

        assertThat(route.id()).isEqualTo(id);
        assertThat(route.calculatedAt()).isEqualTo(calculatedAt);
    }

    @Test
    void equality_isIdentityByRouteId() {
        RouteId id = RouteId.generate();
        Instant t = Instant.parse("2026-01-15T08:30:00Z");

        Route a = Route.reconstitute(id, origin, destination, 5.5, Duration.ofMinutes(15), t);
        Route b = Route.reconstitute(id, origin, destination, 99.9, Duration.ofMinutes(99), t.plusSeconds(10));

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
