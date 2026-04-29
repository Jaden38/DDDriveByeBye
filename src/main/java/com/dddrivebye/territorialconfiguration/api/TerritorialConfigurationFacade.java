package com.dddrivebye.territorialconfiguration.api;

import com.dddrivebye.territorialconfiguration.api.dto.TerritoryDto;
import com.dddrivebye.territorialconfiguration.application.command.CreateTerritoryCommand;
import com.dddrivebye.territorialconfiguration.application.command.DeactivateTerritoryCommand;
import com.dddrivebye.territorialconfiguration.application.command.UpdateTerritorialRulesCommand;
import com.dddrivebye.territorialconfiguration.application.handler.TerritoryCommandHandler;
import com.dddrivebye.territorialconfiguration.application.handler.TerritoryQueryHandler;
import com.dddrivebye.territorialconfiguration.application.query.GetRulesForTerritoryQuery;
import com.dddrivebye.territorialconfiguration.application.query.GetTerritoryForCoordinatesQuery;
import com.dddrivebye.territorialconfiguration.application.query.IsCoordinatesCoveredQuery;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the territorial-configuration bounded context.
 * Other modules MUST depend on this class only.
 */
@Component
public class TerritorialConfigurationFacade {

    private final TerritoryCommandHandler commandHandler;
    private final TerritoryQueryHandler queryHandler;

    public TerritorialConfigurationFacade(TerritoryCommandHandler commandHandler,
                                          TerritoryQueryHandler queryHandler) {
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
    }

    public UUID createTerritory(CreateTerritoryCommand command) {
        return commandHandler.handle(command);
    }

    public void updateTerritorialRules(UpdateTerritorialRulesCommand command) {
        commandHandler.handle(command);
    }

    public void deactivateTerritory(UUID territoryId) {
        commandHandler.handle(new DeactivateTerritoryCommand(territoryId));
    }

    public Optional<TerritoryDto> getRulesForTerritory(UUID territoryId) {
        return queryHandler.handle(new GetRulesForTerritoryQuery(territoryId));
    }

    public Optional<TerritoryDto> getTerritoryForCoordinates(double latitude, double longitude) {
        return queryHandler.handle(new GetTerritoryForCoordinatesQuery(latitude, longitude));
    }

    public boolean isCoordinatesCovered(double latitude, double longitude) {
        return queryHandler.handle(new IsCoordinatesCoveredQuery(latitude, longitude));
    }
}
