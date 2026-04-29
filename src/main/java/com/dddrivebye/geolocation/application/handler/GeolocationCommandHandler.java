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
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeolocationCommandHandler {

    private final DriverPositionRepository driverPositions;
    private final RouteRepository routes;
    private final RoutingService routingService;
    private final DomainEventPublisher eventPublisher;

    public GeolocationCommandHandler(DriverPositionRepository driverPositions,
                                     RouteRepository routes,
                                     RoutingService routingService,
                                     DomainEventPublisher eventPublisher) {
        this.driverPositions = driverPositions;
        this.routes = routes;
        this.routingService = routingService;
        this.eventPublisher = eventPublisher;
    }

    public void handle(UpdateDriverPositionCommand command) {
        DriverId driverId = DriverId.of(command.driverId());
        GeoCoordinates coordinates = GeoCoordinates.of(command.latitude(), command.longitude());
        RealTimePosition position = RealTimePosition.capture(driverId, coordinates);
        driverPositions.save(position);
        eventPublisher.publish(new DriverPositionUpdatedEvent(driverId, coordinates));
    }

    public void handle(RemoveDriverPositionCommand command) {
        driverPositions.remove(DriverId.of(command.driverId()));
    }

    @Transactional
    public RouteDto handle(CalculateRouteCommand command) {
        GeoCoordinates origin = GeoCoordinates.of(command.originLatitude(), command.originLongitude());
        GeoCoordinates destination = GeoCoordinates.of(command.destinationLatitude(), command.destinationLongitude());
        RoutingService.RouteCalculation calc = routingService.calculateRoute(origin, destination);
        Route route = Route.create(origin, destination, calc.distanceKm(), calc.duration());
        routes.save(route);
        return new RouteDto(
                route.id().value(),
                origin.latitude(), origin.longitude(),
                destination.latitude(), destination.longitude(),
                route.distanceKm(),
                route.duration().toMinutes(),
                route.calculatedAt());
    }
}
