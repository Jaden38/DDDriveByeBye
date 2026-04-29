package com.dddrivebye.territorialconfiguration.api;

import com.dddrivebye.territorialconfiguration.api.dto.TerritoryDto;
import com.dddrivebye.territorialconfiguration.application.command.CreateTerritoryCommand;
import com.dddrivebye.territorialconfiguration.application.command.UpdateTerritorialRulesCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/territories")
public class TerritoryController {

    private final TerritorialConfigurationFacade facade;

    public TerritoryController(TerritorialConfigurationFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateRequest req) {
        List<CreateTerritoryCommand.RegulatoryConstraintInput> constraints = req.constraints() == null
                ? List.of()
                : req.constraints().stream()
                        .map(c -> new CreateTerritoryCommand.RegulatoryConstraintInput(c.code(), c.description()))
                        .toList();
        UUID id = facade.createTerritory(new CreateTerritoryCommand(
                req.name(), req.centerLatitude(), req.centerLongitude(), req.radiusKm(),
                req.perKilometerRate(), req.perMinuteRate(), req.pickupFee(),
                req.maxSurgeCoefficient(), req.cancellationFee(),
                req.carpoolingGroupingEnabled(), req.fixedFare(), req.currency(), constraints));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerritoryDto> getById(@PathVariable UUID id) {
        return facade.getRulesForTerritory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/covering")
    public ResponseEntity<TerritoryDto> getCovering(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return facade.getTerritoryForCoordinates(latitude, longitude)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/covered")
    public ResponseEntity<CoverageResponse> isCovered(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        boolean covered = facade.isCoordinatesCovered(latitude, longitude);
        return ResponseEntity.ok(new CoverageResponse(covered));
    }

    @PutMapping("/{id}/rules")
    public ResponseEntity<Void> updateRules(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateRulesRequest req) {
        facade.updateTerritorialRules(new UpdateTerritorialRulesCommand(
                id, req.perKilometerRate(), req.perMinuteRate(), req.pickupFee(),
                req.maxSurgeCoefficient(), req.cancellationFee(),
                req.carpoolingGroupingEnabled(), req.fixedFare(), req.currency()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        facade.deactivateTerritory(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateRequest(
            @NotBlank String name,
            double centerLatitude,
            double centerLongitude,
            @Positive double radiusKm,
            @NotNull BigDecimal perKilometerRate,
            @NotNull BigDecimal perMinuteRate,
            @NotNull BigDecimal pickupFee,
            @NotNull BigDecimal maxSurgeCoefficient,
            @NotNull BigDecimal cancellationFee,
            boolean carpoolingGroupingEnabled,
            BigDecimal fixedFare,
            @NotBlank String currency,
            List<ConstraintInput> constraints
    ) {
        public record ConstraintInput(@NotBlank String code, @NotBlank String description) {}
    }

    public record UpdateRulesRequest(
            @NotNull BigDecimal perKilometerRate,
            @NotNull BigDecimal perMinuteRate,
            @NotNull BigDecimal pickupFee,
            @NotNull BigDecimal maxSurgeCoefficient,
            @NotNull BigDecimal cancellationFee,
            boolean carpoolingGroupingEnabled,
            BigDecimal fixedFare,
            @NotBlank String currency
    ) {}

    public record CoverageResponse(boolean covered) {}
}
