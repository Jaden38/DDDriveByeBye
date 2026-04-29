package com.dddrivebye.geolocation.application.handler;

import com.dddrivebye.geolocation.domain.repository.DriverPositionRepository;
import com.dddrivebye.geolocation.domain.repository.RouteRepository;
import com.dddrivebye.geolocation.domain.service.RoutingService;
import com.dddrivebye.geolocation.domain.valueobject.DriverId;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.usermanagement.domain.event.DriverAvailabilityChangedEvent;
import com.dddrivebye.usermanagement.domain.valueobject.AvailabilityStatus;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DriverAvailabilityEventHandlerTest {

    private DriverPositionRepository driverPositions;
    private DriverAvailabilityEventHandler listener;

    @BeforeEach
    void setUp() {
        driverPositions = mock(DriverPositionRepository.class);
        RouteRepository routes = mock(RouteRepository.class);
        RoutingService routingService = mock(RoutingService.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);
        GeolocationCommandHandler commandHandler = new GeolocationCommandHandler(
                driverPositions, routes, routingService, eventPublisher);
        listener = new DriverAvailabilityEventHandler(commandHandler);
    }

    @Test
    void offlineTransition_removesDriverPosition() {
        UUID userUuid = UUID.randomUUID();
        DriverAvailabilityChangedEvent event = new DriverAvailabilityChangedEvent(
                UserId.of(userUuid),
                AvailabilityStatus.AVAILABLE,
                AvailabilityStatus.OFFLINE);

        listener.on(event);

        ArgumentCaptor<DriverId> removed = ArgumentCaptor.forClass(DriverId.class);
        verify(driverPositions).remove(removed.capture());
        assertThat(removed.getValue().value()).isEqualTo(userUuid);
    }

    @Test
    void availableTransition_isIgnored() {
        DriverAvailabilityChangedEvent event = new DriverAvailabilityChangedEvent(
                UserId.of(UUID.randomUUID()),
                AvailabilityStatus.OFFLINE,
                AvailabilityStatus.AVAILABLE);

        listener.on(event);

        verify(driverPositions, never()).remove(any());
    }

    @Test
    void onRideTransition_isIgnored() {
        DriverAvailabilityChangedEvent event = new DriverAvailabilityChangedEvent(
                UserId.of(UUID.randomUUID()),
                AvailabilityStatus.AVAILABLE,
                AvailabilityStatus.ON_RIDE);

        listener.on(event);

        verify(driverPositions, never()).remove(any());
    }
}
