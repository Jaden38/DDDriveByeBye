package com.dddrivebye.geolocation.infrastructure.adapter;

import com.dddrivebye.geolocation.domain.service.RoutingService;
import com.dddrivebye.geolocation.domain.valueobject.Eta;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MockRoutingAdapterTest {

    private final MockRoutingAdapter adapter = new MockRoutingAdapter();
    private final GeoCoordinates paris = GeoCoordinates.of(48.8566, 2.3522);
    private final GeoCoordinates lyon = GeoCoordinates.of(45.7640, 4.8357);

    @Test
    void calculateRoute_distanceMatchesHaversine() {
        RoutingService.RouteCalculation calc = adapter.calculateRoute(paris, lyon);

        assertThat(calc.distanceKm()).isEqualTo(paris.distanceKmTo(lyon));
        assertThat(calc.distanceKm()).isBetween(390.0, 400.0);
    }

    @Test
    void calculateRoute_durationFollowsAverageSpeed30Kmh() {
        RoutingService.RouteCalculation calc = adapter.calculateRoute(paris, lyon);

        long expectedMinutes = Math.round(calc.distanceKm() / 30.0 * 60);
        assertThat(calc.duration()).isEqualTo(Duration.ofMinutes(expectedMinutes));
    }

    @Test
    void calculateRoute_durationFloorIsOneMinuteForVeryShortRoutes() {
        GeoCoordinates a = GeoCoordinates.of(48.8566, 2.3522);
        GeoCoordinates b = GeoCoordinates.of(48.8567, 2.3523);

        RoutingService.RouteCalculation calc = adapter.calculateRoute(a, b);

        assertThat(calc.duration()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void estimateEta_isFixedAt5Minutes() {
        Eta eta = adapter.estimateEta(paris, lyon);

        assertThat(eta.toMinutes()).isEqualTo(5);
    }
}
