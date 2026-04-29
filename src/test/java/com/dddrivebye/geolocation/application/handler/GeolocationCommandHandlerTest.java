package com.dddrivebye.geolocation.application.handler;

import com.dddrivebye.geolocation.api.dto.RouteDto;
import com.dddrivebye.geolocation.application.command.CalculateRouteCommand;
import com.dddrivebye.geolocation.application.command.RemoveDriverPositionCommand;
import com.dddrivebye.geolocation.application.command.UpdateDriverPositionCommand;
import com.dddrivebye.geolocation.domain.entity.RealTimePosition;
import com.dddrivebye.geolocation.domain.entity.Route;
import com.dddrivebye.geolocation.domain.event.DriverPositionUpdatedEvent;
import com.dddrivebye.geolocation.domain.repository.DriverPositionRepository;
import com.dddrivebye.geolocation.domain.repository.RouteRepository;
import com.dddrivebye.geolocation.domain.service.RoutingService;
import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeolocationCommandHandlerTest {

    private DriverPositionRepository driverPositions;
    private RouteRepository routes;
    private RoutingService routingService;
    private DomainEventPublisher eventPublisher;
    private GeolocationCommandHandler handler;

    @BeforeEach
    void setUp() {
        driverPositions = mock(DriverPositionRepository.class);
        routes = mock(RouteRepository.class);
        routingService = mock(RoutingService.class);
        eventPublisher = mock(DomainEventPublisher.class);
        handler = new GeolocationCommandHandler(driverPositions, routes, routingService, eventPublisher);
    }

    @Test
    void updateDriverPosition_savesPositionAndPublishesEvent() {
        UUID driverUuid = UUID.randomUUID();
        UpdateDriverPositionCommand cmd = new UpdateDriverPositionCommand(driverUuid, 48.8566, 2.3522);

        handler.handle(cmd);

        ArgumentCaptor<RealTimePosition> savedPosition = ArgumentCaptor.forClass(RealTimePosition.class);
        verify(driverPositions).save(savedPosition.capture());
        assertThat(savedPosition.getValue().driverId().value()).isEqualTo(driverUuid);
        assertThat(savedPosition.getValue().coordinates().latitude()).isEqualTo(48.8566);
        assertThat(savedPosition.getValue().coordinates().longitude()).isEqualTo(2.3522);

        ArgumentCaptor<BaseDomainEvent> publishedEvent = ArgumentCaptor.forClass(BaseDomainEvent.class);
        verify(eventPublisher).publish(publishedEvent.capture());
        assertThat(publishedEvent.getValue()).isInstanceOf(DriverPositionUpdatedEvent.class);
        DriverPositionUpdatedEvent event = (DriverPositionUpdatedEvent) publishedEvent.getValue();
        assertThat(event.driverId().value()).isEqualTo(driverUuid);
        assertThat(event.coordinates().latitude()).isEqualTo(48.8566);
    }

    @Test
    void updateDriverPosition_withInvalidLatitude_throwsAndDoesNotPersist() {
        UpdateDriverPositionCommand cmd = new UpdateDriverPositionCommand(UUID.randomUUID(), 999.0, 0.0);

        assertThat(catchThrowable(() -> handler.handle(cmd)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(driverPositions, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void removeDriverPosition_delegatesToRepository() {
        UUID driverUuid = UUID.randomUUID();

        handler.handle(new RemoveDriverPositionCommand(driverUuid));

        ArgumentCaptor<DriverId> removed = ArgumentCaptor.forClass(DriverId.class);
        verify(driverPositions).remove(removed.capture());
        assertThat(removed.getValue().value()).isEqualTo(driverUuid);
    }

    @Test
    void calculateRoute_persistsRouteAndReturnsDto() {
        CalculateRouteCommand cmd = new CalculateRouteCommand(48.8566, 2.3522, 45.7640, 4.8357);
        when(routingService.calculateRoute(any(), any()))
                .thenReturn(new RoutingService.RouteCalculation(395.0, Duration.ofMinutes(790)));

        RouteDto dto = handler.handle(cmd);

        ArgumentCaptor<Route> savedRoute = ArgumentCaptor.forClass(Route.class);
        verify(routes).save(savedRoute.capture());
        assertThat(savedRoute.getValue().distanceKm()).isEqualTo(395.0);
        assertThat(savedRoute.getValue().duration()).isEqualTo(Duration.ofMinutes(790));

        assertThat(dto.id()).isEqualTo(savedRoute.getValue().id().value());
        assertThat(dto.originLatitude()).isEqualTo(48.8566);
        assertThat(dto.destinationLongitude()).isEqualTo(4.8357);
        assertThat(dto.distanceKm()).isEqualTo(395.0);
        assertThat(dto.durationMinutes()).isEqualTo(790);
    }

    private static Throwable catchThrowable(Runnable r) {
        try {
            r.run();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
}
