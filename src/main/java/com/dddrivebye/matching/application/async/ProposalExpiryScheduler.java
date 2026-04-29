package com.dddrivebye.matching.application.async;

import com.dddrivebye.matching.api.MatchingFacade;
import com.dddrivebye.matching.domain.event.MatchProposalSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Schedules a delayed expiry check for every active proposal — the BullMQ
 * "delayed job" equivalent. When the 30-second window closes, fires
 * MatchingFacade.expireProposal(rideId), which transitions the match back to
 * SEARCHING (or to UNMATCHED at the rematch ceiling).
 *
 * In-memory schedule: timers are lost on JVM restart. Acceptable for the
 * single-instance modular monolith; replace with Redis-backed delayed jobs
 * (or BullMQ proper, if/when we go polyglot) when the deployment grows.
 */
@Component
public class ProposalExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProposalExpiryScheduler.class);

    private final MatchingFacade matching;
    private final TaskScheduler scheduler;

    public ProposalExpiryScheduler(MatchingFacade matching,
                                   @Qualifier("matchingTaskScheduler") TaskScheduler scheduler) {
        this.matching = matching;
        this.scheduler = scheduler;
    }

    @EventListener
    public void on(MatchProposalSentEvent event) {
        UUID rideId = event.rideId();
        scheduler.schedule(() -> expire(rideId), event.expiresAt());
    }

    private void expire(UUID rideId) {
        try {
            matching.expireProposal(rideId);
        } catch (RuntimeException ex) {
            // Either the driver already accepted/declined (proposal no longer
            // active) or the match was cancelled. Both are normal; log at debug.
            log.debug("Proposal expiry skipped for ride {}: {}", rideId, ex.getMessage());
        }
    }
}
