package com.dddrivebye.geolocation.application.handler;

import com.dddrivebye.geolocation.api.dto.DriverPositionDto;
import com.dddrivebye.geolocation.api.dto.EtaDto;
import com.dddrivebye.geolocation.api.dto.NearbyDriverDto;
import com.dddrivebye.geolocation.application.query.GetDriverPositionQuery;
import com.dddrivebye.geolocation.application.query.GetDriversWithinRadiusQuery;
import com.dddrivebye.geolocation.application.query.GetEtaQuery;
import com.dddrivebye.geolocation.domain.entity.RealTimePosition;
import com.dddrivebye.geolocation.domain.repository.DriverPositionRepository;
import com.dddrivebye.geolocation.domain.repository.DriverPositionRepository.DriverWithDistance;
import com.dddrivebye.geolocation.domain.service.RoutingService;
import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.geolocation.domain.valueobject.Eta;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeolocationQueryHandlerTest {

    private DriverPositionRepository driverPositions;
    private RoutingService routingService;
    private GeolocationQueryHandler handler;

    @BeforeEach
    void setUp() {
        driverPositions = mock(DriverPositionRepository.class);
        routingService = mock(RoutingService.class);
        handler = new GeolocationQueryHandler(driverPositions, routingService);
    }

    @Test
    void getDriversWithinRadius_mapsRepositoryResultToDtos() {
        UUID driverUuid = UUID.randomUUID();
        when(driverPositions.findWithinRadius(any(), eq(5.0)))
                .thenReturn(List.of(new DriverWithDistance(
                        DriverId.of(driverUuid),
                        GeoCoordinates.of(48.86, 2.35),
                        1.2)));

        List<NearbyDriverDto> result = handler.handle(new GetDriversWithinRadiusQuery(48.8566, 2.3522, 5.0));

        assertThat(result).hasSize(1);
        NearbyDriverDto dto = result.get(0);
        assertThat(dto.driverId()).isEqualTo(driverUuid);
        assertThat(dto.latitude()).isEqualTo(48.86);
        assertThat(dto.longitude()).isEqualTo(2.35);
        assertThat(dto.distanceKm()).isEqualTo(1.2);
    }

    @Test
    void getDriversWithinRadius_emptyResultReturnsEmptyList() {
        when(driverPositions.findWithinRadius(any(), any(Double.class))).thenReturn(List.of());

        List<NearbyDriverDto> result = handler.handle(new GetDriversWithinRadiusQuery(0.0, 0.0, 1.0));

        assertThat(result).isEmpty();
    }

    @Test
    void getEta_delegatesToRoutingServiceAndReturnsMinutes() {
        when(routingService.estimateEta(any(), any())).thenReturn(Eta.ofMinutes(5));

        EtaDto dto = handler.handle(new GetEtaQuery(48.8566, 2.3522, 45.7640, 4.8357));

        assertThat(dto.minutes()).isEqualTo(5);
    }

    @Test
    void getDriverPosition_returnsEmptyWhenRepositoryHasNothing() {
        when(driverPositions.findByDriverId(any())).thenReturn(Optional.empty());

        Optional<DriverPositionDto> result = handler.handle(new GetDriverPositionQuery(UUID.randomUUID()));

        assertThat(result).isEmpty();
    }

    @Test
    void getDriverPosition_mapsExistingPosition() {
        UUID driverUuid = UUID.randomUUID();
        Instant capturedAt = Instant.parse("2026-01-15T08:30:00Z");
        RealTimePosition position = RealTimePosition.reconstitute(
                DriverId.of(driverUuid),
                GeoCoordinates.of(48.8566, 2.3522),
                capturedAt);
        when(driverPositions.findByDriverId(any())).thenReturn(Optional.of(position));

        Optional<DriverPositionDto> result = handler.handle(new GetDriverPositionQuery(driverUuid));

        assertThat(result).isPresent();
        DriverPositionDto dto = result.orElseThrow();
        assertThat(dto.driverId()).isEqualTo(driverUuid);
        assertThat(dto.latitude()).isEqualTo(48.8566);
        assertThat(dto.longitude()).isEqualTo(2.3522);
        assertThat(dto.capturedAt()).isEqualTo(capturedAt);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
