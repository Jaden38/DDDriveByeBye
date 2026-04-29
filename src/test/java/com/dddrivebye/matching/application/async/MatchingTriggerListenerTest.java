package com.dddrivebye.matching.application.async;

import com.dddrivebye.matching.api.MatchingFacade;
import com.dddrivebye.matching.application.command.RunImmediateMatchingCommand;
import com.dddrivebye.ridemanagement.domain.event.RideRequestedEvent;
import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.territorialconfiguration.api.TerritorialConfigurationFacade;
import com.dddrivebye.territorialconfiguration.api.dto.TerritoryDto;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingTriggerListenerTest {

    private static final GeoCoordinates PICKUP = GeoCoordinates.of(48.8566, 2.3522);
    private static final GeoCoordinates DESTINATION = GeoCoordinates.of(48.8606, 2.3376);

    @Mock private MatchingFacade matching;
    @Mock private TerritorialConfigurationFacade territories;

    private MatchingTriggerListener listener;

    @BeforeEach
    void setUp() {
        listener = new MatchingTriggerListener(matching, territories);
    }

    @Test
    void shouldDispatchMatchingWhenNoTerritoryCovers() {
        UUID rideId = UUID.randomUUID();
        RideRequestedEvent event = event(rideId);
        when(territories.getTerritoryForCoordinates(anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());

        listener.on(event);

        ArgumentCaptor<RunImmediateMatchingCommand> captor =
                ArgumentCaptor.forClass(RunImmediateMatchingCommand.class);
        verify(matching).runImmediateMatching(captor.capture());
        RunImmediateMatchingCommand cmd = captor.getValue();
        assertThat(cmd.rideId()).isEqualTo(rideId);
        assertThat(cmd.pickupLatitude()).isEqualTo(PICKUP.latitude());
        assertThat(cmd.pickupLongitude()).isEqualTo(PICKUP.longitude());
        assertThat(cmd.territoryId()).isNull();
        assertThat(cmd.territoryRequiresVtcLicense()).isFalse();
        assertThat(cmd.requiredOptions()).isEmpty();
        assertThat(cmd.passengerHasPet()).isFalse();
    }

    @Test
    void shouldFlagVtcRequirementWhenTerritoryHasConstraint() {
        UUID rideId = UUID.randomUUID();
        UUID territoryId = UUID.randomUUID();
        TerritoryDto territory = territory(territoryId,
                List.of(new TerritoryDto.RegulatoryConstraintDto(
                        "VTC_LICENSE", "Drivers must hold a VTC license")));
        when(territories.getTerritoryForCoordinates(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(territory));

        listener.on(event(rideId));

        ArgumentCaptor<RunImmediateMatchingCommand> captor =
                ArgumentCaptor.forClass(RunImmediateMatchingCommand.class);
        verify(matching).runImmediateMatching(captor.capture());
        assertThat(captor.getValue().territoryId()).isEqualTo(territoryId);
        assertThat(captor.getValue().territoryRequiresVtcLicense()).isTrue();
    }

    @Test
    void shouldNotFlagVtcWhenTerritoryHasOtherConstraints() {
        TerritoryDto territory = territory(UUID.randomUUID(),
                List.of(new TerritoryDto.RegulatoryConstraintDto(
                        "NIGHT_SURCHARGE", "Higher fares 22:00-06:00")));
        when(territories.getTerritoryForCoordinates(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(territory));

        listener.on(event(UUID.randomUUID()));

        ArgumentCaptor<RunImmediateMatchingCommand> captor =
                ArgumentCaptor.forClass(RunImmediateMatchingCommand.class);
        verify(matching).runImmediateMatching(captor.capture());
        assertThat(captor.getValue().territoryRequiresVtcLicense()).isFalse();
    }

    @Test
    void shouldSwallowMatchingFailuresInsteadOfPropagating() {
        when(territories.getTerritoryForCoordinates(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("territorial service down"));

        // Must not propagate — the @Async method has no caller to receive it.
        listener.on(event(UUID.randomUUID()));

        verify(matching, never()).runImmediateMatching(org.mockito.ArgumentMatchers.any());
    }

    private RideRequestedEvent event(UUID rideId) {
        return new RideRequestedEvent(
                RideId.of(rideId),
                UserId.of(UUID.randomUUID()),
                PICKUP,
                DESTINATION,
                2);
    }

    private TerritoryDto territory(UUID id, List<TerritoryDto.RegulatoryConstraintDto> constraints) {
        TerritoryDto.TerritorialRuleDto rule = new TerritoryDto.TerritorialRuleDto(
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, true, null, "EUR");
        return new TerritoryDto(id, "Paris", PICKUP.latitude(), PICKUP.longitude(),
                10.0, true, rule, constraints);
    }
}
