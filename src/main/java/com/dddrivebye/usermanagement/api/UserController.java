package com.dddrivebye.usermanagement.api;

import com.dddrivebye.usermanagement.api.dto.AvailableDriverDto;
import com.dddrivebye.usermanagement.api.dto.DriverProfileDto;
import com.dddrivebye.usermanagement.api.dto.UserDto;
import com.dddrivebye.usermanagement.application.command.AddDriverProfileCommand;
import com.dddrivebye.usermanagement.application.command.DefineActivityZoneCommand;
import com.dddrivebye.usermanagement.application.command.DefineWorkingZoneCommand;
import com.dddrivebye.usermanagement.application.command.RegisterIndividualAccountCommand;
import com.dddrivebye.usermanagement.application.command.RegisterProfessionalAccountCommand;
import com.dddrivebye.usermanagement.application.command.RegisterVehicleCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementFacade facade;

    public UserController(UserManagementFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/individual")
    public ResponseEntity<Void> registerIndividual(@Valid @RequestBody IndividualRegistrationRequest req) {
        UUID id = facade.registerIndividualAccount(
                new RegisterIndividualAccountCommand(req.fullName(), req.email(), req.phoneNumber()));
        return created(id);
    }

    @PostMapping("/professional")
    public ResponseEntity<Void> registerProfessional(@Valid @RequestBody ProfessionalRegistrationRequest req) {
        UUID id = facade.registerProfessionalAccount(new RegisterProfessionalAccountCommand(
                req.fullName(), req.email(), req.phoneNumber(),
                req.vtcLicenseNumber(), req.vtcLicenseExpiry(),
                req.driversLicenseNumber(), req.driversLicenseExpiry(),
                req.insuranceCompany(), req.insurancePolicyNumber(), req.insuranceExpiry(),
                req.vehicleMake(), req.vehicleModel(), req.vehicleYear(), req.vehicleLicensePlate()));
        return created(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return facade.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/driver-profile")
    public ResponseEntity<DriverProfileDto> getDriverProfile(@PathVariable UUID id) {
        return facade.getDriverProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/driver-profile")
    public ResponseEntity<Void> addDriverProfile(@PathVariable UUID id,
                                                  @Valid @RequestBody AddDriverProfileRequest req) {
        facade.addDriverProfile(new AddDriverProfileCommand(
                id,
                req.driversLicenseNumber(), req.driversLicenseExpiry(),
                req.insuranceCompany(), req.insurancePolicyNumber(), req.insuranceExpiry(),
                req.vehicleMake(), req.vehicleModel(), req.vehicleYear(), req.vehicleLicensePlate()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/driver-profile/approve")
    public ResponseEntity<Void> approveDriverProfile(@PathVariable UUID id) {
        facade.approveDriverProfile(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/driver-profile/reject")
    public ResponseEntity<Void> rejectDriverProfile(@PathVariable UUID id,
                                                     @RequestBody RejectRequest req) {
        facade.rejectDriverProfile(id, req.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/vehicles")
    public ResponseEntity<Void> registerVehicle(@PathVariable UUID id,
                                                 @Valid @RequestBody VehicleRequest req) {
        UUID vehicleId = facade.registerVehicle(new RegisterVehicleCommand(
                id, req.make(), req.model(), req.year(), req.licensePlate(),
                req.seats(), req.fuelType(),
                req.options() == null ? java.util.Set.of() : new java.util.HashSet<>(req.options())));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{vehicleId}").buildAndExpand(vehicleId).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}/availability/activate")
    public ResponseEntity<Void> activateAvailability(@PathVariable UUID id,
                                                      @RequestParam(defaultValue = "false") boolean hasActivePassengerRide) {
        facade.activateAvailability(id, hasActivePassengerRide);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/availability/deactivate")
    public ResponseEntity<Void> deactivateAvailability(@PathVariable UUID id) {
        facade.deactivateAvailability(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activity-zone")
    public ResponseEntity<Void> defineActivityZone(@PathVariable UUID id,
                                                    @Valid @RequestBody ZoneRequest req) {
        facade.defineActivityZone(new DefineActivityZoneCommand(id, req.label(), req.latitude(), req.longitude(), req.radiusKm()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/working-zone")
    public ResponseEntity<Void> defineWorkingZone(@PathVariable UUID id,
                                                   @Valid @RequestBody ZoneRequest req) {
        facade.defineWorkingZone(new DefineWorkingZoneCommand(id, req.label(), req.latitude(), req.longitude(), req.radiusKm()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/drivers/available")
    public ResponseEntity<List<AvailableDriverDto>> getAvailableDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam(required = false) String accountType) {
        return ResponseEntity.ok(facade.getAvailableDriversNear(latitude, longitude, radiusKm, accountType));
    }

    private ResponseEntity<Void> created(UUID id) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    public record IndividualRegistrationRequest(
            @NotBlank String fullName,
            @NotBlank String email,
            @NotBlank String phoneNumber) {}

    public record ProfessionalRegistrationRequest(
            @NotBlank String fullName,
            @NotBlank String email,
            @NotBlank String phoneNumber,
            @NotBlank String vtcLicenseNumber,
            @NotNull LocalDate vtcLicenseExpiry,
            @NotBlank String driversLicenseNumber,
            @NotNull LocalDate driversLicenseExpiry,
            @NotBlank String insuranceCompany,
            @NotBlank String insurancePolicyNumber,
            @NotNull LocalDate insuranceExpiry,
            @NotBlank String vehicleMake,
            @NotBlank String vehicleModel,
            @NotNull Integer vehicleYear,
            @NotBlank String vehicleLicensePlate) {}

    public record AddDriverProfileRequest(
            @NotBlank String driversLicenseNumber,
            @NotNull LocalDate driversLicenseExpiry,
            @NotBlank String insuranceCompany,
            @NotBlank String insurancePolicyNumber,
            @NotNull LocalDate insuranceExpiry,
            @NotBlank String vehicleMake,
            @NotBlank String vehicleModel,
            @NotNull Integer vehicleYear,
            @NotBlank String vehicleLicensePlate) {}

    public record RejectRequest(@NotBlank String reason) {}

    public record VehicleRequest(
            @NotBlank String make,
            @NotBlank String model,
            @NotNull Integer year,
            @NotBlank String licensePlate,
            @Positive int seats,
            @NotBlank String fuelType,
            java.util.Set<String> options) {}

    public record ZoneRequest(
            @NotBlank String label,
            double latitude,
            double longitude,
            @Positive double radiusKm) {}
}
