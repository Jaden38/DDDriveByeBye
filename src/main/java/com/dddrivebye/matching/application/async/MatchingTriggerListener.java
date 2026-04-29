package com.dddrivebye.matching.application.async;

import com.dddrivebye.matching.api.MatchingFacade;
import com.dddrivebye.matching.application.command.RunImmediateMatchingCommand;
import com.dddrivebye.matching.domain.valueobject.RideOption;
import com.dddrivebye.ridemanagement.domain.event.RideRequestedEvent;
import com.dddrivebye.territorialconfiguration.api.TerritorialConfigurationFacade;
import com.dddrivebye.territorialconfiguration.api.dto.TerritoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Async consumer for ride-management's RideRequestedEvent — the matching
 * module's "queue worker" entry point. Runs on {@code matchingTaskExecutor},
 * not the request thread.
 *
 * The Ride aggregate currently doesn't carry ride options, pet preference, or
 * search radius, so the listener supplies sensible defaults; once those
 * attributes land on Ride, lift them into the event payload.
 */
@Component
public class MatchingTriggerListener {

    private static final Logger log = LoggerFactory.getLogger(MatchingTriggerListener.class);

    private static final String VTC_LICENSE_CODE = "VTC_LICENSE";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;

    private final MatchingFacade matching;
    private final TerritorialConfigurationFacade territories;

    public MatchingTriggerListener(MatchingFacade matching,
                                   TerritorialConfigurationFacade territories) {
        this.matching = matching;
        this.territories = territories;
    }

    @Async("matchingTaskExecutor")
    @EventListener
    public void on(RideRequestedEvent event) {
        UUID rideId = event.rideId().value();
        try {
            Optional<TerritoryDto> territory = territories.getTerritoryForCoordinates(
                    event.pickupPoint().latitude(),
                    event.pickupPoint().longitude());

            UUID territoryId = territory.map(TerritoryDto::id).orElse(null);
            boolean vtcRequired = territory.map(this::requiresVtcLicense).orElse(false);

            matching.runImmediateMatching(new RunImmediateMatchingCommand(
                    rideId,
                    event.pickupPoint().latitude(),
                    event.pickupPoint().longitude(),
                    DEFAULT_SEARCH_RADIUS_KM,
                    Set.<RideOption>of(),
                    /*passengerHasPet*/ false,
                    territoryId,
                    vtcRequired));
        } catch (RuntimeException ex) {
            // @Async swallows exceptions by default — log explicitly so we can
            // diagnose failed matching jobs.
            log.error("Matching job failed for ride {}", rideId, ex);
        }
    }

    private boolean requiresVtcLicense(TerritoryDto territory) {
        return territory.constraints().stream()
                .anyMatch(c -> VTC_LICENSE_CODE.equalsIgnoreCase(c.code()));
    }
}
