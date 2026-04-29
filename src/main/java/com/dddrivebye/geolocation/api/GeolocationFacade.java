package com.dddrivebye.geolocation.api;

import com.dddrivebye.geolocation.api.dto.DriverPositionDto;
import com.dddrivebye.geolocation.api.dto.EtaDto;
import com.dddrivebye.geolocation.api.dto.NearbyDriverDto;
import com.dddrivebye.geolocation.api.dto.RouteDto;
import com.dddrivebye.geolocation.application.command.CalculateRouteCommand;
import com.dddrivebye.geolocation.application.command.RemoveDriverPositionCommand;
import com.dddrivebye.geolocation.application.command.UpdateDriverPositionCommand;
import com.dddrivebye.geolocation.application.handler.GeolocationCommandHandler;
import com.dddrivebye.geolocation.application.handler.GeolocationQueryHandler;
import com.dddrivebye.geolocation.application.query.GetDriverPositionQuery;
import com.dddrivebye.geolocation.application.query.GetDriversWithinRadiusQuery;
import com.dddrivebye.geolocation.application.query.GetEtaQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the geolocation bounded context.
 * Other modules MUST depend on this class only — never on internal handlers,
 * repositories or domain types.
 */
@Component
public class GeolocationFacade {

    private final GeolocationCommandHandler commandHandler;
    private final GeolocationQueryHandler queryHandler;

    public GeolocationFacade(GeolocationCommandHandler commandHandler,
                             GeolocationQueryHandler queryHandler) {
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
    }

    public void updateDriverPosition(UUID driverId, double latitude, double longitude) {
        commandHandler.handle(new UpdateDriverPositionCommand(driverId, latitude, longitude));
    }

    public void removeDriverPosition(UUID driverId) {
        commandHandler.handle(new RemoveDriverPositionCommand(driverId));
    }

    public RouteDto calculateRoute(double originLatitude,
                                   double originLongitude,
                                   double destinationLatitude,
                                   double destinationLongitude) {
        return commandHandler.handle(new CalculateRouteCommand(
                originLatitude, originLongitude, destinationLatitude, destinationLongitude));
    }

    public EtaDto getEta(double fromLatitude,
                         double fromLongitude,
                         double toLatitude,
                         double toLongitude) {
        return queryHandler.handle(new GetEtaQuery(fromLatitude, fromLongitude, toLatitude, toLongitude));
    }

    public List<NearbyDriverDto> getDriversWithinRadius(double latitude,
                                                       double longitude,
                                                       double radiusKm) {
        return queryHandler.handle(new GetDriversWithinRadiusQuery(latitude, longitude, radiusKm));
    }

    public Optional<DriverPositionDto> getDriverPosition(UUID driverId) {
        return queryHandler.handle(new GetDriverPositionQuery(driverId));
    }
}
