package com.dddrivebye.matching.infrastructure.adapters;

import com.dddrivebye.matching.application.port.ReputationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Placeholder adapter — to be replaced once the reputation module exists.
 * Returns a neutral reputation score (5.0) and no active restrictions, so
 * matching can run end-to-end without a reputation back-end.
 */
@Component
@Profile("!reputation-real")
public class StubReputationAdapter implements ReputationPort {

    private static final double NEUTRAL_SCORE = 5.0;

    @Override
    public double reputationScore(UUID driverId) {
        return NEUTRAL_SCORE;
    }

    @Override
    public boolean hasActiveRestriction(UUID driverId) {
        return false;
    }
}
